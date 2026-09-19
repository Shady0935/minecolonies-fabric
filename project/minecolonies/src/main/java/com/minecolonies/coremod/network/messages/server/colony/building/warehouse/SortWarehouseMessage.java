package com.minecolonies.coremod.network.messages.server.colony.building.warehouse;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.coremod.util.SortingUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.capability.ForgeCapabilities;
import com.minecolonies.fabric.network.NetworkEvent;

/**
 * Sort the warehouse if level bigger than 3.
 */
public class SortWarehouseMessage extends AbstractBuildingServerMessage<BuildingWareHouse>
{
    /**
     * The required level to sort a warehouse.
     */
    private static final int REQUIRED_LEVEL_TO_SORT_WAREHOUSE = 3;

    /**
     * Empty constructor used when registering the
     */
    public SortWarehouseMessage()
    {
        super();
    }

    public SortWarehouseMessage(final IBuildingView building)
    {
        super(building);
    }

    /** Creates a server-bound request without requiring a client building view. */
    public SortWarehouseMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId)
    {
        super(dimensionId, colonyId, buildingId);
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {

    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {

    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingWareHouse building)
    {
        if (building.getBuildingLevel() >= REQUIRED_LEVEL_TO_SORT_WAREHOUSE)
        {
            building.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(inv -> SortingUtils.sort((CombinedItemHandler) inv));
        }
    }
}
