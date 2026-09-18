package com.minecolonies.fabric.common.extensions;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

public final class IForgeMenuType
{
    @FunctionalInterface
    public interface CreateMenu<T extends AbstractContainerMenu>
    {
        T create(int windowId, Inventory inventory, FriendlyByteBuf data);
    }

    private IForgeMenuType()
    {
    }

    public static <T extends AbstractContainerMenu> MenuType<T> create(final CreateMenu<T> factory)
    {
        return new ExtendedScreenHandlerType<>(factory::create);
    }
}
