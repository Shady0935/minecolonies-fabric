package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.IOPool;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Common Fabric event registrations replacing the Forge event bus hooks. */
public final class EventSubscriber
{
    private static boolean registered;

    private EventSubscriber()
    {
    }

    public static synchronized void registerCommon()
    {
        if (registered)
        {
            return;
        }
        registered = true;

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
          EntryPoint.register(dispatcher, environment));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final ServerPlayer player = handler.getPlayer();
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), player);
        });

        ServerTickEvents.START_WORLD_TICK.register(level -> {
            BlockUtils.checkOrInit();
            Manager.onWorldTick(level);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> IOPool.shutdown());
    }
}
