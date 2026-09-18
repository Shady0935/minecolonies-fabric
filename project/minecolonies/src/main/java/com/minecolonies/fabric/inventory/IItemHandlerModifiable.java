package com.minecolonies.fabric.inventory;

import net.minecraft.world.item.ItemStack;

public interface IItemHandlerModifiable extends IItemHandler
{
    void setStackInSlot(int slot, ItemStack stack);
}
