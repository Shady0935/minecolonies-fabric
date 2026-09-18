package com.minecolonies.fabric.inventory;

import net.minecraft.world.item.ItemStack;

public interface IItemHandler extends com.ldtteam.structurize.api.util.IItemHandler
{
    int getSlots();

    ItemStack getStackInSlot(int slot);

    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
