package com.ldtteam.structurize.storage.rendering;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.network.messages.SyncPreviewCacheToClient;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.ldtteam.structurize.api.util.constant.Constants.DISPLAY_SHARED;

/** Handles blueprint preview syncing between players. */
public final class ServerPreviewDistributor
{
    private static final Map<UUID, Tuple<ServerPlayer, BlueprintRenderSettings>> registeredPlayers = new HashMap<>();
    private static boolean registered;

    private ServerPreviewDistributor()
    {
    }

    public static synchronized void register()
    {
        if (registered)
        {
            return;
        }
        registered = true;
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final ServerPlayer player = handler.getPlayer();
            registeredPlayers.put(player.getUUID(), new Tuple<>(player, BlueprintRenderSettings.instance));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
          registeredPlayers.remove(handler.getPlayer().getUUID()));
    }

    public static void distribute(final BlueprintPreviewData renderingCache, final Player player)
    {
        for (final Tuple<ServerPlayer, BlueprintRenderSettings> entry : registeredPlayers.values())
        {
            if (entry.getB().renderSettings.get(DISPLAY_SHARED)
                  && entry.getA().isAlive()
                  && player.level() == entry.getA().level()
                  && !player.getUUID().equals(entry.getA().getUUID())
                  && (entry.getA().blockPosition().distSqr(renderingCache.getPos()) < 128 * 128 || renderingCache.getPos().equals(BlockPos.ZERO)))
            {
                Network.getNetwork().sendToPlayer(new SyncPreviewCacheToClient(renderingCache, entry.getA().getUUID().toString()), entry.getA());
            }
        }
    }

    public static void register(final UUID uuid, final Tuple<ServerPlayer, BlueprintRenderSettings> settings)
    {
        registeredPlayers.put(uuid, settings);
    }
}
