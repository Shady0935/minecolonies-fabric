package com.minecolonies.fabric.inventory;

import net.minecraft.world.item.ItemStack;

/** Presents several item handlers as one contiguous handler. */
public class CombinedInvWrapper implements IItemHandlerModifiable
{
    private final IItemHandler[] handlers;
    private final int[] offsets;

    public CombinedInvWrapper(final IItemHandler... handlers)
    {
        this.handlers = handlers.clone();
        this.offsets = new int[handlers.length];
        int offset = 0;
        for (int i = 0; i < handlers.length; i++)
        {
            offsets[i] = offset;
            offset += handlers[i].getSlots();
        }
    }

    @Override
    public int getSlots()
    {
        return offsets.length == 0 ? 0 : offsets[offsets.length - 1] + handlers[handlers.length - 1].getSlots();
    }

    private int handlerIndex(final int slot)
    {
        if (slot < 0 || slot >= getSlots())
        {
            return -1;
        }
        for (int i = handlers.length - 1; i >= 0; i--)
        {
            if (slot >= offsets[i])
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        final int index = handlerIndex(slot);
        return index < 0 ? ItemStack.EMPTY : handlers[index].getStackInSlot(slot - offsets[index]);
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        final int index = handlerIndex(slot);
        if (index < 0)
        {
            return;
        }
        final int localSlot = slot - offsets[index];
        if (handlers[index] instanceof IItemHandlerModifiable modifiable)
        {
            modifiable.setStackInSlot(localSlot, stack);
        }
        else
        {
            final ItemStack existing = handlers[index].extractItem(localSlot, Integer.MAX_VALUE, false);
            if (!existing.isEmpty())
            {
                handlers[index].insertItem(localSlot, stack, false);
            }
            else
            {
                handlers[index].insertItem(localSlot, stack, false);
            }
        }
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        final int index = handlerIndex(slot);
        return index < 0 ? stack : handlers[index].insertItem(slot - offsets[index], stack, simulate);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        final int index = handlerIndex(slot);
        return index < 0 ? ItemStack.EMPTY : handlers[index].extractItem(slot - offsets[index], amount, simulate);
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        final int index = handlerIndex(slot);
        return index < 0 ? 0 : handlers[index].getSlotLimit(slot - offsets[index]);
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        final int index = handlerIndex(slot);
        return index >= 0 && handlers[index].isItemValid(slot - offsets[index], stack);
    }
}
