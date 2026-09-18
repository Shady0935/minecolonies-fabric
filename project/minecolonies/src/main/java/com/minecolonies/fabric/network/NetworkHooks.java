package com.minecolonies.fabric.network;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;

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

    public static void openScreen(final ServerPlayer player, final MenuProvider provider, final Consumer<FriendlyByteBuf> writer)
    {
        if (writer == null)
        {
            player.openMenu(provider);
            return;
        }

        // Fabric's ExtendedScreenHandlerFactory is the direct equivalent of
        // Forge's NetworkHooks extra opening buffer.  Keeping the original
        // provider as the delegate preserves the server-side menu creation,
        // while Fabric forwards the same bytes to the client's
        // ExtendedScreenHandlerType factory before the screen is constructed.
        player.openMenu(new ExtendedScreenHandlerFactory()
        {
            @Override
            public Component getDisplayName()
            {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(final int windowId, final Inventory inventory, final Player openingPlayer)
            {
                return provider.createMenu(windowId, inventory, openingPlayer);
            }

            @Override
            public void writeScreenOpeningData(final ServerPlayer openingPlayer, final FriendlyByteBuf buffer)
            {
                writer.accept(buffer);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static Packet<ClientGamePacketListener> getEntitySpawningPacket(final Entity entity)
    {
        return (Packet<ClientGamePacketListener>) (Packet<?>) new net.minecraft.network.protocol.game.ClientboundAddEntityPacket(entity);
    }
}
