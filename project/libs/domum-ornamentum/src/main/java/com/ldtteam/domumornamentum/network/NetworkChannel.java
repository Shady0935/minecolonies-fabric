package com.ldtteam.domumornamentum.network;

import com.ldtteam.domumornamentum.network.messages.CreativeSetArchitectCutterSlotMessage;
import com.ldtteam.domumornamentum.network.messages.IMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import static com.ldtteam.domumornamentum.util.Constants.MOD_ID;

/** Fabric networking facade retaining the public API used by the cutter screen and JEI integration. */
public final class NetworkChannel
{
    private final ResourceLocation channelId;
    private boolean registered;

    public NetworkChannel(final String channelName)
    {
        this.channelId = new ResourceLocation(MOD_ID, channelName);
    }

    public synchronized void registerMessages()
    {
        if (registered)
        {
            return;
        }
        registered = true;

        ServerPlayNetworking.registerGlobalReceiver(channelId, (server, player, handler, buf, responseSender) -> {
            final CreativeSetArchitectCutterSlotMessage message = new CreativeSetArchitectCutterSlotMessage(buf);
            server.execute(() -> message.apply(player));
        });
    }

    public void sendToServer(final IMessage message)
    {
        final FriendlyByteBuf buffer = PacketByteBufs.create();
        message.toBytes(buffer);
        ClientPlayNetworking.send(channelId, buffer);
    }

    public void sendToPlayer(final IMessage message, final ServerPlayer player)
    {
        final FriendlyByteBuf buffer = PacketByteBufs.create();
        message.toBytes(buffer);
        ServerPlayNetworking.send(player, channelId, buffer);
    }
}
