package com.minecolonies.fabric.client.network;

import com.ldtteam.structurize.config.fabric.ConfigSnapshotCodec;
import com.minecolonies.fabric.config.FabricConfiguration;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;

/** Client-only network entrypoint; common configuration code never loads Minecraft client classes. */
public final class ClientConfigurationHooks
{
    private ClientConfigurationHooks() { }

    public static void register()
    {
        ClientPlayNetworking.registerGlobalReceiver(FabricConfiguration.CHANNEL, (client, handler, buffer, sender) -> {
            final String json = buffer.readUtf(ConfigSnapshotCodec.MAX_LENGTH);
            client.execute(() -> {
                // Integrated servers share the live objects; a network override must not replace their local snapshot.
                if (client.getSingleplayerServer() != null) { return; }
                try { FabricConfiguration.applyRemote(json); }
                catch (final RuntimeException exception)
                {
                    org.slf4j.LoggerFactory.getLogger("minecolonies/config").error("Rejected invalid server configuration", exception);
                    handler.getConnection().disconnect(Component.literal("MineColonies: invalid or incompatible server configuration. Update both client and server."));
                }
            });
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(FabricConfiguration::restoreLocal));
    }
}
