package com.ldtteam.structurize.api.util;

import net.minecraft.world.item.ItemStack;

/**
 * Small platform-neutral inventory contract used by Structurize placement.
 *
 * <p>Forge's IItemHandler is deliberately not exposed by the Fabric port.
 * Implementations should return a remainder from {@link #insertItem} and a
 * newly extracted stack from {@link #extractItem}.</p>
 */
public interface IItemHandler
{
    int getSlots();

    ItemStack getStackInSlot(int slot);

    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
