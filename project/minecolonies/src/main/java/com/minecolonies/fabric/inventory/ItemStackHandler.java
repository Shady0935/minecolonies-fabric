package com.minecolonies.fabric.inventory;

import com.minecolonies.fabric.util.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

/** Simple, server-safe inventory handler equivalent to Forge's ItemStackHandler. */
public class ItemStackHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag>
{
    protected ItemStack[] stacks;

    public ItemStackHandler()
    {
        this(0);
    }

    public ItemStackHandler(final int size)
    {
        stacks = new ItemStack[size];
        Arrays.fill(stacks, ItemStack.EMPTY);
    }

    @Override
    public int getSlots() { return stacks.length; }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        return slot >= 0 && slot < stacks.length ? stacks[slot] : ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        if (slot < 0 || slot >= stacks.length) return;
        stacks[slot] = stack == null ? ItemStack.EMPTY : stack.copy();
        onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        if (stack == null || stack.isEmpty() || slot < 0 || slot >= stacks.length || !isItemValid(slot, stack)) return stack;
        final ItemStack current = stacks[slot];
        if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, stack)) return stack;
        final int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        final int room = limit - (current.isEmpty() ? 0 : current.getCount());
        if (room <= 0) return stack;
        final int moved = Math.min(room, stack.getCount());
        if (!simulate)
        {
            final ItemStack next = current.isEmpty() ? stack.copy() : current.copy();
            next.setCount((current.isEmpty() ? 0 : current.getCount()) + moved);
            stacks[slot] = next;
            onContentsChanged(slot);
        }
        if (moved == stack.getCount()) return ItemStack.EMPTY;
        return stack.copyWithCount(stack.getCount() - moved);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        if (amount <= 0 || slot < 0 || slot >= stacks.length || stacks[slot].isEmpty()) return ItemStack.EMPTY;
        final ItemStack current = stacks[slot];
        final int moved = Math.min(amount, current.getCount());
        final ItemStack result = current.copyWithCount(moved);
        if (!simulate)
        {
            current.shrink(moved);
            if (current.isEmpty()) stacks[slot] = ItemStack.EMPTY;
            onContentsChanged(slot);
        }
        return result;
    }

    @Override
    public int getSlotLimit(final int slot) { return 64; }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack) { return true; }

    protected void onContentsChanged(final int slot)
    {
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag result = new CompoundTag();
        final ListTag list = new ListTag();
        for (int i = 0; i < stacks.length; i++)
        {
            if (!stacks[i].isEmpty())
            {
                final CompoundTag slot = new CompoundTag();
                slot.putInt("Slot", i);
                slot.put("Stack", stacks[i].save(new CompoundTag()));
                list.add(slot);
            }
        }
        result.put("Items", list);
        return result;
    }

    @Override
    public void deserializeNBT(final CompoundTag tag)
    {
        Arrays.fill(stacks, ItemStack.EMPTY);
        final ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundTag slot = list.getCompound(i);
            final int index = slot.getInt("Slot");
            if (index >= 0 && index < stacks.length) stacks[index] = ItemStack.of(slot.getCompound("Stack"));
        }
    }
}
