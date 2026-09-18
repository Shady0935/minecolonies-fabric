package com.ldtteam.structurize.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.network.messages.*;
import com.ldtteam.structurize.network.messages.splitting.SplitPacketMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Fabric networking facade for Structurize.
 *
 * <p>The upstream channel uses Forge's SimpleChannel and wraps every payload in
 * a split packet. Fabric exposes a packet id instead of a channel id, so the
 * split wrapper is registered on one stable Fabric identifier and the original
 * Structurize message table is kept in memory exactly as before.</p>
 */
public class NetworkChannel
{
    private final String channelName;
    private final Map<Integer, NetworkingMessageEntry<?>> messagesTypes = Maps.newHashMap();
    private final Map<Class<? extends IMessage>, Integer> messageTypeToIdMap = Maps.newHashMap();
    private final Cache<Integer, Map<Integer, byte[]>> messageCache = CacheBuilder.newBuilder()
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .concurrencyLevel(8)
      .build();
    private final AtomicInteger messageCounter = new AtomicInteger();
    private volatile MinecraftServer server;
    private boolean commonRegistered;
    private boolean serverReceiverRegistered;
    private boolean clientReceiverRegistered;

    public NetworkChannel(final String channelName)
    {
        this.channelName = channelName;
        ServerLifecycleEvents.SERVER_STARTED.register(startedServer -> this.server = startedServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(stoppingServer -> {
            if (this.server == stoppingServer)
            {
                this.server = null;
            }
        });
    }

    /** Registers the common message table and the server-side receiver. */
    public synchronized void registerCommonMessages()
    {
        if (!commonRegistered)
        {
            commonRegistered = true;

            int idx = 0;
            registerMessage(++idx, RemoveBlockMessage.class, RemoveBlockMessage::new);
            registerMessage(++idx, RemoveEntityMessage.class, RemoveEntityMessage::new);
            registerMessage(++idx, SaveScanMessage.class, SaveScanMessage::new);
            registerMessage(++idx, ReplaceBlockMessage.class, ReplaceBlockMessage::new);
            registerMessage(++idx, FillTopPlaceholderMessage.class, FillTopPlaceholderMessage::new);
            registerMessage(++idx, ScanOnServerMessage.class, ScanOnServerMessage::new);
            registerMessage(++idx, ServerUUIDMessage.class, ServerUUIDMessage::new);
            registerMessage(++idx, UndoRedoMessage.class, UndoRedoMessage::new);
            registerMessage(++idx, UpdateScanToolMessage.class, UpdateScanToolMessage::new);
            registerMessage(++idx, UpdateClientRender.class, UpdateClientRender::new);
            registerMessage(++idx, BuildToolPlacementMessage.class, BuildToolPlacementMessage::new);
            registerMessage(++idx, ShowScanMessage.class, ShowScanMessage::new);

            registerMessage(++idx, AddRemoveTagMessage.class, AddRemoveTagMessage::new);
            registerMessage(++idx, SetTagInTool.class, SetTagInTool::new);
            registerMessage(++idx, OperationHistoryMessage.class, OperationHistoryMessage::new);

            registerMessage(++idx, NotifyServerAboutStructurePacksMessage.class, NotifyServerAboutStructurePacksMessage::new);
            // The Forge source registered BuildToolPlacementMessage twice. Keep
            // the numeric slot for compatibility without registering it twice.
            idx++;
            registerMessage(++idx, BlueprintSyncMessage.class, BlueprintSyncMessage::new);
            registerMessage(++idx, SyncSettingsToServer.class, SyncSettingsToServer::new);
            registerMessage(++idx, SyncPreviewCacheToServer.class, SyncPreviewCacheToServer::new);

            registerMessage(++idx, NotifyClientAboutStructurePacksMessage.class, NotifyClientAboutStructurePacksMessage::new);
            registerMessage(++idx, TransferStructurePackToClient.class, TransferStructurePackToClient::new);
            registerMessage(++idx, ClientBlueprintRequestMessage.class, ClientBlueprintRequestMessage::new);
            registerMessage(++idx, SyncPreviewCacheToClient.class, SyncPreviewCacheToClient::new);

            registerMessage(++idx, ItemMiddleMouseMessage.class, ItemMiddleMouseMessage::new);
            registerMessage(++idx, ScanToolTeleportMessage.class, ScanToolTeleportMessage::new);
            registerMessage(++idx, AbsorbBlockMessage.class, AbsorbBlockMessage::new);
        }

        registerServerReceiver();
    }

    /** Registers the client-side receiver from the Fabric client entrypoint. */
    public synchronized void registerClientMessages()
    {
        if (!commonRegistered)
        {
            registerCommonMessages();
        }

        if (clientReceiverRegistered)
        {
            return;
        }
        clientReceiverRegistered = true;

        ClientPlayNetworking.registerGlobalReceiver(splitChannelId(), (client, handler, buf, responseSender) -> {
            final SplitPacketMessage message = new SplitPacketMessage(buf);
            client.execute(() -> message.onExecute(new NetworkEvent.Context(null, LogicalSide.SERVER), false));
        });
    }

    private void registerServerReceiver()
    {
        if (serverReceiverRegistered)
        {
            return;
        }
        serverReceiverRegistered = true;

        ServerPlayNetworking.registerGlobalReceiver(splitChannelId(), (server, player, handler, buf, responseSender) -> {
            final SplitPacketMessage message = new SplitPacketMessage(buf);
            server.execute(() -> message.onExecute(new NetworkEvent.Context(player, LogicalSide.CLIENT), true));
        });
    }

    private ResourceLocation splitChannelId()
    {
        return new ResourceLocation(Constants.MOD_ID, channelName + "_split");
    }

    private <MSG extends IMessage> void registerMessage(final int id,
                                                        final Class<MSG> msgClazz,
                                                        final Function<FriendlyByteBuf, MSG> msgCreator)
    {
        this.messagesTypes.put(id, new NetworkingMessageEntry<>(msgCreator, msgClazz));
        this.messageTypeToIdMap.put(msgClazz, id);
    }

    public void sendToServer(final IMessage msg)
    {
        handleSplitting(msg, packet -> {
            final FriendlyByteBuf buffer = PacketByteBufs.create();
            packet.toBytes(buffer);
            ClientPlayNetworking.send(splitChannelId(), buffer);
        });
    }

    public void sendToPlayer(final IMessage msg, final ServerPlayer player)
    {
        handleSplitting(msg, packet -> {
            final FriendlyByteBuf buffer = PacketByteBufs.create();
            packet.toBytes(buffer);
            ServerPlayNetworking.send(player, splitChannelId(), buffer);
        });
    }

    public void sendToOrigin(final IMessage msg, final NetworkEvent.Context ctx)
    {
        final ServerPlayer player = ctx.getSender();
        if (player != null)
        {
            sendToPlayer(msg, player);
        }
        else
        {
            sendToServer(msg);
        }
    }

    public void sendToDimension(final IMessage msg, final ResourceKey<Level> dimension)
    {
        final MinecraftServer currentServer = this.server;
        if (currentServer == null)
        {
            return;
        }
        final ServerLevel level = currentServer.getLevel(dimension);
        if (level != null)
        {
            PlayerLookup.world(level).forEach(player -> sendToPlayer(msg, player));
        }
    }

    public void sendToEveryone(final IMessage msg)
    {
        final MinecraftServer currentServer = this.server;
        if (currentServer != null)
        {
            PlayerLookup.all(currentServer).forEach(player -> sendToPlayer(msg, player));
        }
    }

    public void sendToTrackingEntity(final IMessage msg, final Entity entity)
    {
        PlayerLookup.tracking(entity).forEach(player -> sendToPlayer(msg, player));
    }

    public void sendToTrackingEntityAndSelf(final IMessage msg, final Entity entity)
    {
        PlayerLookup.tracking(entity).forEach(player -> sendToPlayer(msg, player));
        if (entity instanceof ServerPlayer player)
        {
            sendToPlayer(msg, player);
        }
    }

    public void sendToTrackingChunk(final IMessage msg, final LevelChunk chunk)
    {
        if (chunk.getLevel() instanceof ServerLevel level)
        {
            PlayerLookup.tracking(level, chunk.getPos()).forEach(player -> sendToPlayer(msg, player));
        }
    }

    private void handleSplitting(final IMessage msg, final Consumer<IMessage> splitMessageConsumer)
    {
        final int messageId = this.messageTypeToIdMap.getOrDefault(msg.getClass(), -1);
        if (messageId == -1)
        {
            throw new IllegalArgumentException("The message is unknown to this channel!");
        }

        final ByteBuf buffer = Unpooled.buffer();
        final FriendlyByteBuf innerFriendlyByteBuf = new FriendlyByteBuf(buffer);
        msg.toBytes(innerFriendlyByteBuf);
        final byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), data);
        buffer.release();

        final int maxPacketSize = msg.getExecutionSide() == LogicalSide.SERVER ? 30000 : 943718;
        int currentIndex = 0;
        int packetIndex = 0;
        final int communicationId = messageCounter.getAndIncrement();

        // Empty payloads still need a terminator packet so the receiver can
        // reconstruct and invoke the message constructor.
        do
        {
            this.getMessagesTypes().get(messageId).onSplitting(packetIndex);
            final int extra = Math.min(maxPacketSize, data.length - currentIndex);
            final byte[] subPacketData = Arrays.copyOfRange(data, currentIndex, currentIndex + extra);
            final SplitPacketMessage splitPacketMessage = new SplitPacketMessage(
              communicationId,
              packetIndex++,
              currentIndex + extra >= data.length,
              messageId,
              subPacketData);
            splitMessageConsumer.accept(splitPacketMessage);
            currentIndex += extra;
        }
        while (currentIndex < data.length);
    }

    public Cache<Integer, Map<Integer, byte[]>> getMessageCache()
    {
        return messageCache;
    }

    public Map<Integer, NetworkingMessageEntry<?>> getMessagesTypes()
    {
        return messagesTypes;
    }

    public static final class NetworkingMessageEntry<MSG extends IMessage>
    {
        private final AtomicBoolean hasWarned = new AtomicBoolean(true);
        private final Function<FriendlyByteBuf, MSG> creator;
        private final Class<? extends IMessage> clazz;

        private NetworkingMessageEntry(final Function<FriendlyByteBuf, MSG> creator,
                                       final Class<? extends IMessage> clazz)
        {
            this.creator = creator;
            this.clazz = clazz;
        }

        public Function<FriendlyByteBuf, MSG> getCreator()
        {
            return creator;
        }

        public void onSplitting(final int packetIndex)
        {
            if (packetIndex != 1)
            {
                return;
            }
            if (hasWarned.getAndSet(false))
            {
                Log.getLogger().warn("Splitting message: " + clazz + " it is too big to send normally. This message is only printed once");
            }
        }
    }
}
