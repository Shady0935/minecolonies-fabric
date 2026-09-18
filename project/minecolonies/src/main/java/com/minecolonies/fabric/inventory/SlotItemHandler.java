package com.minecolonies.fabric.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Container slot backed by an item handler rather than a vanilla container. */
public class SlotItemHandler extends Slot
{
    protected final IItemHandler itemHandler;
    protected final int index;

    public SlotItemHandler(final IItemHandler itemHandler, final int index, final int x, final int y)
    {
        super(new SimpleContainer(0), 0, x, y);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public ItemStack getItem()
    {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public boolean hasItem()
    {
        return !getItem().isEmpty();
    }

    @Override
    public void set(final ItemStack stack)
    {
        if (itemHandler instanceof IItemHandlerModifiable modifiable)
        {
            modifiable.setStackInSlot(index, stack);
        }
        else
        {
            itemHandler.extractItem(index, Integer.MAX_VALUE, false);
            itemHandler.insertItem(index, stack, false);
        }
    }

    @Override
    public void setByPlayer(final ItemStack stack)
    {
        set(stack);
    }

    @Override
    public ItemStack remove(final int amount)
    {
        return itemHandler.extractItem(index, amount, false);
    }

    @Override
    public boolean mayPlace(final ItemStack stack)
    {
        return itemHandler.isItemValid(index, stack);
    }

    @Override
    public int getMaxStackSize()
    {
        return itemHandler.getSlotLimit(index);
    }

    @Override
    public int getMaxStackSize(final ItemStack stack)
    {
        return Math.min(getMaxStackSize(), stack.getMaxStackSize());
    }

    @Override
    public boolean mayPickup(final Player player)
    {
        return true;
    }
}
