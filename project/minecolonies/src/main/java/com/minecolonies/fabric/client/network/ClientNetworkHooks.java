package com.minecolonies.fabric.client.network;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.util.ChunkCapData;
import com.minecolonies.coremod.util.ChunkClientDataHelper;
import com.minecolonies.coremod.util.FurnaceRecipes;
import com.minecolonies.coremod.datalistener.QuestJsonListener;
import com.minecolonies.fabric.network.simple.SimpleChannel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

import java.util.HashSet;
import java.util.Set;

/** Client-only Fabric boundary for the common MineColonies network facade. */
public final class ClientNetworkHooks
{
    private static final Set<ResourceLocation> REGISTERED = new HashSet<>();

    private ClientNetworkHooks()
    {
    }

    public static void register(final SimpleChannel channel)
    {
        if (!REGISTERED.add(channel.id()))
        {
            return;
        }

        final boolean registered = ClientPlayNetworking.registerGlobalReceiver(channel.id(),
          (client, handler, buffer, responseSender) -> channel.handleClient(buffer, client::execute));
        if (!registered && !ClientPlayNetworking.getGlobalReceivers().contains(channel.id()))
        {
            throw new IllegalStateException("Unable to register MineColonies client packet channel " + channel.id());
        }
        Log.getLogger().debug("Registered MineColonies client packet channel {}", channel.id());
    }

    /** Reflection target used by the common channel when a client sends C2S. */
    public static void send(final ResourceLocation channel, final FriendlyByteBuf buffer)
    {
        ClientPlayNetworking.send(channel, buffer);
    }

    /** Apply the client-side colony view without leaking client classes into common messages. */
    public static void handleColonyViewMessage(final int colonyId,
                                               final FriendlyByteBuf colonyBuffer,
                                               final boolean isNewSubscription,
                                               final ResourceKey<Level> dimension)
    {
        if (Minecraft.getInstance().level != null)
        {
            IColonyManager.getInstance().handleColonyViewMessage(colonyId, colonyBuffer,
              Minecraft.getInstance().level, isNewSubscription, dimension);
        }
    }

    /** Apply one server-sent chunk capability update on the client. */
    public static void handleUpdateChunkCapabilityMessage(final ChunkCapData chunkCapData)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        if (!WorldUtil.isChunkLoaded(world, new ChunkPos(chunkCapData.x, chunkCapData.z)))
        {
            ChunkClientDataHelper.addCapData(chunkCapData);
            return;
        }

        final LevelChunk chunk = world.getChunk(chunkCapData.x, chunkCapData.z);
        final IColonyTagCapability cap = com.minecolonies.fabric.capability.CapabilityHooks
          .getCapability(chunk, com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP, null).orElse(null);
        if (cap != null && cap.getOwningColony() != chunkCapData.owningColony)
        {
            ChunkClientDataHelper.applyCap(chunkCapData, chunk);
        }
    }

    /** Apply a server-sent range of chunk capability updates on the client. */
    public static void handleUpdateChunkRangeCapabilityMessage(final List<ChunkCapData> caps)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        for (final ChunkCapData data : caps)
        {
            if (!WorldUtil.isChunkLoaded(world, new ChunkPos(data.x, data.z)))
            {
                ChunkClientDataHelper.addCapData(data);
                continue;
            }

            final LevelChunk chunk = world.getChunk(data.x, data.z);
            ChunkClientDataHelper.applyCap(data, chunk);
        }
    }

    /** Apply the server compatibility snapshot on the client. */
    public static void handleUpdateClientWithCompatibilityMessage(final FriendlyByteBuf buffer)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        FurnaceRecipes.getInstance().loadUtilityPredicates();
        try
        {
            IColonyManager.getInstance().getCompatibilityManager().deserialize(buffer, world);
        }
        catch (final Exception exception)
        {
            Log.getLogger().error("Failed to load compatibility manager", exception);
        }
    }

    /** Apply the server quest snapshot on the client. */
    public static void handleGlobalQuestSyncMessage(final FriendlyByteBuf buffer)
    {
        if (Minecraft.getInstance().level != null)
        {
            QuestJsonListener.readGlobalQuestPackets(buffer);
        }
    }
}
