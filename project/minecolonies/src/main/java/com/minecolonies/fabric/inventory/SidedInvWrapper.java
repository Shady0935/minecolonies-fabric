package com.minecolonies.fabric.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

/** Maps exposed face slots and honours vanilla automation insertion/extraction rules. */
public final class SidedInvWrapper implements IItemHandlerModifiable
{
    private final WorldlyContainer container;
    private final Direction side;
    private final InvWrapper delegate;

    public SidedInvWrapper(final WorldlyContainer container, final Direction side)
    {
        this.container = container;
        this.side = side;
        this.delegate = new InvWrapper(container);
    }

    private int slot(final int index)
    {
        final int[] slots = container.getSlotsForFace(side);
        return index >= 0 && index < slots.length ? slots[index] : -1;
    }

    @Override public int getSlots() { return container.getSlotsForFace(side).length; }
    @Override public ItemStack getStackInSlot(final int index) { return delegate.getStackInSlot(slot(index)); }
    @Override public int getSlotLimit(final int index) { return delegate.getSlotLimit(slot(index)); }
    @Override public void setStackInSlot(final int index, final ItemStack stack) { delegate.setStackInSlot(slot(index), stack); }
    @Override public boolean isItemValid(final int index, final ItemStack stack) { return delegate.isItemValid(slot(index), stack); }

    @Override public ItemStack insertItem(final int index, final ItemStack stack, final boolean simulate)
    {
        final int actual = slot(index);
        return actual >= 0 && container.canPlaceItemThroughFace(actual, stack, side)
            ? delegate.insertItem(actual, stack, simulate) : stack;
    }

    @Override public ItemStack extractItem(final int index, final int amount, final boolean simulate)
    {
        final int actual = slot(index);
        return actual >= 0 && container.canTakeItemThroughFace(actual, container.getItem(actual), side)
            ? delegate.extractItem(actual, amount, simulate) : ItemStack.EMPTY;
    }
}
