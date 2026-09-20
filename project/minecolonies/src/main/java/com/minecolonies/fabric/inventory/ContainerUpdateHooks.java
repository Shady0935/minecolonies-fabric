package com.minecolonies.fabric.inventory;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Bridges vanilla container mutations to the MineColonies request lifecycle. */
public final class ContainerUpdateHooks
{
    private ContainerUpdateHooks()
    {
    }

    /**
     * Re-evaluate deliverable requests when a registered vanilla container receives items.
     *
     * @param blockEntity the changed container block entity.
     */
    public static void notifyRegisteredContainerChanged(final BlockEntity blockEntity)
    {
        if (!(blockEntity instanceof Container container))
        {
            return;
        }

        final Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide)
        {
            return;
        }

        final List<ItemStack> contents = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++)
        {
            final ItemStack stack = container.getItem(slot);
            if (!ItemStackUtils.isEmpty(stack))
            {
                contents.add(stack.copy());
            }
        }

        if (contents.isEmpty())
        {
            return;
        }

        final var containerPos = blockEntity.getBlockPos();
        for (final IColony colony : IColonyManager.getInstance().getColonies(level))
        {
            for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
            {
                if (!building.getContainers().contains(containerPos))
                {
                    continue;
                }

                colony.getRequestManager().onColonyUpdate(request ->
                    request.getRequest() instanceof IDeliverable deliverable
                        && contents.stream().anyMatch(deliverable::matches));
                return;
            }
        }
    }
}
