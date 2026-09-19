package com.minecolonies.fabric.network.simple;

import com.minecolonies.api.util.Log;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import com.minecolonies.fabric.network.PacketDistributor;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fabric transport backing the small Forge-shaped channel API used by the
 * MineColonies 1.20.1 source tree.
 *
 * <p>MineColonies already has a stable message id and fragmentation envelope.
 * Fabric therefore carries one play channel ({@link #id}) and this class
 * delegates decoding of that envelope to the registered message handler. The
 * envelope is registered once for both directions; inner message ids remain
 * owned by {@code NetworkChannel}.</p>
 */
public final class SimpleChannel
{
    private static final String CLIENT_BRIDGE = "com.minecolonies.fabric.client.network.ClientNetworkHooks";

    private final ResourceLocation id;
    private final Map<Integer, Registration<?>> registrations = new HashMap<>();
    private final Map<Class<?>, Registration<?>> registrationsByType = new HashMap<>();
    private boolean serverReceiverRegistered;

    public SimpleChannel(final ResourceLocation id)
    {
        this.id = Objects.requireNonNull(id, "id");
    }

    public <MSG> void registerMessage(final int messageId,
                                      final Class<MSG> messageType,
                                      final BiConsumer<MSG, FriendlyByteBuf> encoder,
                                      final Function<FriendlyByteBuf, MSG> decoder,
                                      final BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer)
    {
        final Registration<MSG> registration = new Registration<>(messageType, encoder, decoder, consumer);
        registrations.put(messageId, registration);
        registrationsByType.put(messageType, registration);
    }

    /** Register the common C2S receiver. Safe to call more than once. */
    public void registerServerReceiver()
    {
        if (serverReceiverRegistered)
        {
            return;
        }

        final boolean registered = ServerPlayNetworking.registerGlobalReceiver(id,
          (server, player, handler, buffer, responseSender) -> handleServerPacket(server, player, buffer));
        if (!registered && !ServerPlayNetworking.getGlobalReceivers().contains(id))
        {
            throw new IllegalStateException("Unable to register MineColonies server packet channel " + id);
        }
        serverReceiverRegistered = true;
    }

    /**
     * Dispatch the single Fabric channel packet on the server network thread.
     *
     * <p>The public boundary is also used by the Fabric GameTest harness so the
     * registered receiver and the deterministic integration fixture exercise the
     * same decode path.</p>
     */
    public void handleServerPacket(final MinecraftServer server, final ServerPlayer sender, final FriendlyByteBuf buffer)
    {
        final Registration<?> registration = registrations.get(0);
        if (registration == null)
        {
            Log.getLogger().error("Received MineColonies packet before the envelope was registered: {}", id);
            return;
        }

        // A packet received by the server originated on the client.  The
        // Forge-shaped split envelope uses this origin to reject messages
        // addressed to the wrong logical side and to expose isLogicalServer.
        final NetworkEvent.Context context = new NetworkEvent.Context(sender, LogicalSide.CLIENT, server::execute);
        decodeAndConsume(registration, buffer, context);
    }

    /** Dispatch a packet received by the client-only Fabric bridge. */
    public void handleClient(final FriendlyByteBuf buffer, final Executor clientExecutor)
    {
        final Registration<?> registration = registrations.get(0);
        if (registration == null)
        {
            Log.getLogger().error("Received MineColonies client packet before the envelope was registered: {}", id);
            return;
        }

        // A packet received by the client originated on the server.
        final NetworkEvent.Context context = new NetworkEvent.Context(null, LogicalSide.SERVER, clientExecutor);
        decodeAndConsume(registration, buffer, context);
    }

    @SuppressWarnings("unchecked")
    private static <MSG> void decodeAndConsume(final Registration<?> rawRegistration,
                                                final FriendlyByteBuf buffer,
                                                final NetworkEvent.Context context)
    {
        final Registration<MSG> registration = (Registration<MSG>) rawRegistration;
        try
        {
            final MSG message = registration.decoder.apply(buffer);
            registration.consumer.accept(message, () -> context);
        }
        catch (final RuntimeException exception)
        {
            Log.getLogger().error("Failed to decode MineColonies packet", exception);
        }
    }

    /** Send a message from a client to the logical server. */
    public void sendToServer(final Object message)
    {
        final byte[] payload = encode(message);
        try
        {
            final Class<?> bridge = Class.forName(CLIENT_BRIDGE);
            bridge.getMethod("send", ResourceLocation.class, FriendlyByteBuf.class)
              .invoke(null, id, new FriendlyByteBuf(Unpooled.wrappedBuffer(payload)));
        }
        catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception)
        {
            throw new IllegalStateException("MineColonies client networking is not available", exception);
        }
    }

    /** Send a message to a Forge-shaped Fabric target descriptor. */
    public void send(final PacketDistributor.PacketTarget target, final Object message)
    {
        if (target == null)
        {
            throw new IllegalArgumentException("Packet target cannot be null");
        }

        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
        {
            Log.getLogger().debug("Skipping MineColonies packet {} because no server is active", id);
            return;
        }

        final byte[] payload = encode(message);
        for (final ServerPlayer player : playersFor(server, target))
        {
            if (player != null && ServerPlayNetworking.canSend(player, id))
            {
                ServerPlayNetworking.send(player, id, new FriendlyByteBuf(Unpooled.wrappedBuffer(Arrays.copyOf(payload, payload.length))));
            }
        }
    }

    private Collection<ServerPlayer> playersFor(final MinecraftServer server, final PacketDistributor.PacketTarget target)
    {
        final Object value = target.value();
        return switch (target.kind())
        {
            case PLAYER -> value instanceof ServerPlayer player ? List.of(player) : List.of();
            case ALL -> PlayerLookup.all(server);
            case DIMENSION -> playersInDimension(server, value);
            case NEAR -> playersNear(server, value);
            case TRACKING_ENTITY -> value instanceof Entity entity ? PlayerLookup.tracking(entity) : List.of();
            case TRACKING_ENTITY_AND_SELF -> playersTrackingEntityAndSelf(value);
            case TRACKING_CHUNK -> playersTrackingChunk(value);
        };
    }

    @SuppressWarnings("unchecked")
    private static Collection<ServerPlayer> playersInDimension(final MinecraftServer server, final Object value)
    {
        if (!(value instanceof ResourceKey<?> key))
        {
            return List.of();
        }
        final ServerLevel level = server.getLevel((ResourceKey<Level>) key);
        return level == null ? List.of() : PlayerLookup.world(level);
    }

    private static Collection<ServerPlayer> playersNear(final MinecraftServer server, final Object value)
    {
        if (!(value instanceof PacketDistributor.TargetPoint point))
        {
            return List.of();
        }
        final ServerLevel level = server.getLevel(point.dimension);
        return level == null ? List.of() : PlayerLookup.around(level, new Vec3(point.x, point.y, point.z), point.range);
    }

    private static Collection<ServerPlayer> playersTrackingEntityAndSelf(final Object value)
    {
        if (!(value instanceof Entity entity))
        {
            return List.of();
        }
        final Collection<ServerPlayer> players = new LinkedHashSet<>(PlayerLookup.tracking(entity));
        if (entity instanceof ServerPlayer player)
        {
            players.add(player);
        }
        return players;
    }

    private static Collection<ServerPlayer> playersTrackingChunk(final Object value)
    {
        if (!(value instanceof LevelChunk chunk) || !(chunk.getLevel() instanceof ServerLevel level))
        {
            return List.of();
        }
        return PlayerLookup.tracking(level, chunk.getPos());
    }

    private byte[] encode(final Object message)
    {
        final Registration<Object> registration = registrationFor(message);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try
        {
            registration.encoder.accept(message, buffer);
            final byte[] payload = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), payload);
            return payload;
        }
        finally
        {
            buffer.release();
        }
    }

    @SuppressWarnings("unchecked")
    private Registration<Object> registrationFor(final Object message)
    {
        if (message == null)
        {
            throw new IllegalArgumentException("MineColonies cannot send a null packet");
        }
        final Registration<?> exact = registrationsByType.get(message.getClass());
        if (exact != null)
        {
            return (Registration<Object>) exact;
        }
        for (final Registration<?> candidate : registrations.values())
        {
            if (candidate.messageType.isInstance(message))
            {
                return (Registration<Object>) candidate;
            }
        }
        throw new IllegalArgumentException("Unknown MineColonies packet type: " + message.getClass().getName());
    }

    public ResourceLocation id()
    {
        return id;
    }

    private record Registration<MSG>(Class<MSG> messageType,
                                     BiConsumer<MSG, FriendlyByteBuf> encoder,
                                     Function<FriendlyByteBuf, MSG> decoder,
                                     BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer)
    {
    }
}
