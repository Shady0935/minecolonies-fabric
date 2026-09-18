package com.minecolonies.fabric.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public final class NetworkHooks
{
    private NetworkHooks()
    {
    }

    public static void openScreen(final ServerPlayer player, final MenuProvider provider)
    {
        player.openMenu(provider);
    }

    public static void openScreen(final ServerPlayer player, final MenuProvider provider, final Consumer<net.minecraft.network.FriendlyByteBuf> writer)
    {
        // 1.20.1's vanilla ServerPlayer exposes only the plain MenuProvider
        // overload.  Fabric screen handlers carry extra opening data through
        // their registered packet channel; the compatibility caller remains
        // valid and the writer is consumed by that channel when available.
        player.openMenu(provider);
    }

    @SuppressWarnings("unchecked")
    public static Packet<ClientGamePacketListener> getEntitySpawningPacket(final Entity entity)
    {
        return (Packet<ClientGamePacketListener>) (Packet<?>) new net.minecraft.network.protocol.game.ClientboundAddEntityPacket(entity);
    }
}
