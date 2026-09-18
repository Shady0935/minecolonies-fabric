package com.ldtteam.structurize.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility methods for the inventories.
 */
public final class ItemStackUtils
{
    /**
     * Private constructor to hide the implicit one.
     */
    private ItemStackUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get itemStack of tileEntityData. Retrieve the data from the tileEntity.
     *
     * @param compound the tileEntity stored in a compound.
     * @param state the block.
     * @return the list of itemstacks.
     */
    public static List<ItemStack> getItemStacksOfTileEntity(final CompoundTag compound, final BlockState state)
    {
        if (state.getBlock() instanceof BaseEntityBlock && compound.contains("Items"))
        {
            // because we're constructing the BlockEntity out-of-world below, chests (and perhaps a few others)
            // can't generate an IItemHandler for us, so we need to read the contents manually.
            // this could be removed if we always get a "real" BE from a world, but we're called both from a
            // real world and from a schematic non-world, and the latter still breaks.
            return getItemStacksFromNbt(compound);
        }

        BlockPos blockpos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
        final BlockEntity tileEntity = BlockEntity.loadStatic(blockpos, state, compound);
        if (tileEntity == null)
        {
            return Collections.emptyList();
        }

        final List<ItemStack> items = new ArrayList<>();
        for (final IItemHandler handler : getItemHandlersFromProvider(tileEntity))
        {
            for (int slot = 0; slot < handler.getSlots(); slot++)
            {
                final ItemStack stack = handler.getStackInSlot(slot);
                if (!ItemStackUtils.isEmpty(stack))
                {
                    items.add(stack);
                }
            }
        }

        return items;
    }

    @NotNull
    private static List<ItemStack> getItemStacksFromNbt(@NotNull final CompoundTag compound)
    {
        final List<ItemStack> items = new ArrayList<>();
        final ListTag listtag = compound.getList("Items", Tag.TAG_COMPOUND);

        for (int i = 0; i < listtag.size(); ++i)
        {
            final CompoundTag compoundtag = listtag.getCompound(i);
            final ItemStack stack = ItemStack.of(compoundtag);
            if (!stack.isEmpty())
            {
                items.add(stack);
            }
        }

        return items;
    }

    /**
     * Method to get all the IItemHandlers from a given Provider.
     *
     * @param provider The block entity to get the IItemHandlers from.
     * @return A list with all the unique IItemHandlers a provider has.
     */
    public static Set<IItemHandler> getItemHandlersFromProvider(final BlockEntity provider)
    {
        final Set<IItemHandler> handlerSet = new HashSet<>();
        if (provider instanceof final net.minecraft.world.Container container)
        {
            handlerSet.add(new ContainerItemHandler(container));
        }
        return handlerSet;
    }

    /** Adapts vanilla Container block entities to the Structurize inventory API. */
    private static final class ContainerItemHandler implements IItemHandler
    {
        private final net.minecraft.world.Container container;

        private ContainerItemHandler(final net.minecraft.world.Container container)
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
            return container.getItem(slot);
        }

        @Override
        public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
        {
            if (stack.isEmpty() || !isItemValid(slot, stack))
            {
                return stack;
            }

            final ItemStack existing = container.getItem(slot);
            final int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, stack))
            {
                return stack;
            }

            final int space = existing.isEmpty() ? limit : limit - existing.getCount();
            if (space <= 0)
            {
                return stack;
            }

            final int inserted = Math.min(space, stack.getCount());
            if (!simulate)
            {
                final ItemStack updated = existing.isEmpty() ? stack.copy() : existing.copy();
                updated.setCount((existing.isEmpty() ? 0 : existing.getCount()) + inserted);
                container.setItem(slot, updated);
                container.setChanged();
            }

            if (inserted == stack.getCount())
            {
                return ItemStack.EMPTY;
            }
            final ItemStack remainder = stack.copy();
            remainder.shrink(inserted);
            return remainder;
        }

        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
        {
            if (amount <= 0)
            {
                return ItemStack.EMPTY;
            }
            final ItemStack existing = container.getItem(slot);
            if (existing.isEmpty())
            {
                return ItemStack.EMPTY;
            }

            final int extracted = Math.min(amount, existing.getCount());
            if (simulate)
            {
                final ItemStack result = existing.copy();
                result.setCount(extracted);
                return result;
            }
            return container.removeItem(slot, extracted);
        }

        @Override
        public int getSlotLimit(final int slot)
        {
            return container.getMaxStackSize();
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack)
        {
            return slot >= 0 && slot < getSlots() && container.canPlaceItem(slot, stack);
        }
    }

    /**
     * Wrapper method to check if a stack is empty.
     * Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    public static boolean isEmpty(@Nullable final ItemStack stack)
    {
        return stack == null || stack.isEmpty() || stack == ItemStack.EMPTY || stack.getCount() <= 0;
    }

    /**
     * get the size of the stack.
     * This is for compatibility between 1.10 and 1.11
     *
     * @param stack to get the size from
     * @return the size of the stack
     */
    public static int getSize(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return 0;
        }

        return stack.getCount();
    }

    /**
     * Get the list of required resources for entities.
     *
     * @param entity the entity object.
     * @param pos the placer pos..
     * @return a list of stacks.
     */
    public static List<ItemStack> getListOfStackForEntity(final Entity entity, final BlockPos pos)
    {
        if (entity != null)
        {
            final List<ItemStack> request = new ArrayList<>();
            if (entity instanceof ItemFrame)
            {
                final ItemStack stack = ((ItemFrame) entity).getItem();
                if (!ItemStackUtils.isEmpty(stack))
                {
                    stack.setCount(1);
                    request.add(stack);
                }
                request.add(new ItemStack(Items.ITEM_FRAME, 1));
            }
            else if (entity instanceof ArmorStand)
            {
                request.add(entity.getPickResult());
                entity.getArmorSlots().forEach(request::add);
                entity.getHandSlots().forEach(request::add);
            }

            return request.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @return True when they are equal except the stacksize, false when not.
     */
    @NotNull
    public static boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2)
    {
        return compareItemStacksIgnoreStackSize(itemStack1, itemStack2, true, true);
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1  The left stack to compare.
     * @param itemStack2  The right stack to compare.
     * @param matchDamage Set to true to match damage data.
     * @param matchNBT    Set to true to match nbt
     * @return True when they are equal except the stacksize, false when not.
     */
    public static boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2, final boolean matchDamage, final boolean matchNBT)
    {
        return compareItemStacksIgnoreStackSize(itemStack1, itemStack2, matchDamage, matchNBT, false);
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1  The left stack to compare.
     * @param itemStack2  The right stack to compare.
     * @param matchDamage Set to true to match damage data.
     * @param matchNBT    Set to true to match nbt
     * @param min         if the count of stack2 has to be at least the same as stack1.
     * @return True when they are equal except the stacksize, false when not.
     */
    public static boolean compareItemStacksIgnoreStackSize(
      final ItemStack itemStack1,
      final ItemStack itemStack2,
      final boolean matchDamage,
      final boolean matchNBT,
      final boolean min)
    {
        if (isEmpty(itemStack1) && isEmpty(itemStack2))
        {
            return true;
        }

        if (isEmpty(itemStack1) != isEmpty(itemStack2))
        {
            return false;
        }

        if (itemStack1.getItem() == itemStack2.getItem() && (!matchDamage || itemStack1.getDamageValue() == itemStack2.getDamageValue()))
        {
            if (!matchNBT)
            {
                // Not comparing nbt
                return true;
            }

            if (min && itemStack1.getCount() > itemStack2.getCount())
            {
                return false;
            }

            // Then sort on NBT
            if (itemStack1.hasTag() && itemStack2.hasTag())
            {
                CompoundTag nbt1 = itemStack1.getTag();
                CompoundTag nbt2 = itemStack2.getTag();

                for(String key :nbt1.getAllKeys())
                {
                    if(!matchDamage && key.equals("Damage"))
                    {
                        continue;
                    }
                    if(!nbt2.contains(key) || !nbt1.get(key).equals(nbt2.get(key)))
                    {
                        return false;
                    }
                }

                return nbt1.getAllKeys().size() == nbt2.getAllKeys().size();
            }
            else
            {
                return (!itemStack1.hasTag() || itemStack1.getTag().isEmpty())
                         && (!itemStack2.hasTag() || itemStack2.getTag().isEmpty());
            }
        }
        return false;
    }
}
