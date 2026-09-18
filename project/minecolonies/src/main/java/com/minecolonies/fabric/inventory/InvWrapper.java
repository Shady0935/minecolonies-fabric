package com.minecolonies.fabric.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Adapts a vanilla container to the item-handler view used by MineColonies. */
public class InvWrapper implements IItemHandlerModifiable
{
    protected final Container container;

    public InvWrapper(final Container container)
    {
        this.container = container;
    }

    @Override
    public int getSlots()
    {
        return container.getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        return slot >= 0 && slot < getSlots() ? container.getItem(slot) : ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        if (slot >= 0 && slot < getSlots())
        {
            container.setItem(slot, stack == null ? ItemStack.EMPTY : stack.copy());
            container.setChanged();
        }
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        if (stack == null || stack.isEmpty() || slot < 0 || slot >= getSlots() || !isItemValid(slot, stack))
        {
            return stack;
        }

        final ItemStack current = getStackInSlot(slot);
        if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, stack))
        {
            return stack;
        }

        final int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        final int room = limit - (current.isEmpty() ? 0 : current.getCount());
        if (room <= 0)
        {
            return stack;
        }

        final int moved = Math.min(room, stack.getCount());
        if (!simulate)
        {
            final ItemStack next = current.isEmpty() ? stack.copy() : current.copy();
            next.setCount((current.isEmpty() ? 0 : current.getCount()) + moved);
            setStackInSlot(slot, next);
        }
        return moved == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        if (amount <= 0 || slot < 0 || slot >= getSlots())
        {
            return ItemStack.EMPTY;
        }

        final ItemStack current = getStackInSlot(slot);
        if (current.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        final int moved = Math.min(amount, current.getCount());
        final ItemStack result = current.copyWithCount(moved);
        if (!simulate)
        {
            container.removeItem(slot, moved);
            container.setChanged();
        }
        return result;
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        return slot >= 0 && slot < getSlots() ? Math.min(64, container.getMaxStackSize()) : 0;
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        return slot >= 0 && slot < getSlots() && container.canPlaceItem(slot, stack);
    }
}
