package com.minecolonies.fabric.client.network;

import com.minecolonies.api.util.Log;
import com.minecolonies.fabric.network.simple.SimpleChannel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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
}
