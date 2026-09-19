package com.minecolonies.coremod.network.messages.server.colony.building.builder;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class BuilderSelectWorkOrderMessage extends AbstractBuildingServerMessage<BuildingBuilder>
{
    private int workOrder;

    /**
     * Empty standard constructor.
     */
    public BuilderSelectWorkOrderMessage()
    {
        super();
    }

    /**
     * Creates a new BuilderSetManualModeMessage.
     *
     * @param building View of the building to read data from.
     * @param workOrder workorder id.
     */
    public BuilderSelectWorkOrderMessage(@NotNull final IBuildingView building, final int workOrder)
    {
        super(building);
        this.workOrder = workOrder;
    }

    /**
     * Creates a server-bound selection message without requiring a client building view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     * @param buildingId builder building position
     * @param workOrder work-order id
     */
    public BuilderSelectWorkOrderMessage(final ResourceKey<Level> dimensionId, final int colonyId,
                                         final BlockPos buildingId, final int workOrder)
    {
        super(dimensionId, colonyId, buildingId);
        this.workOrder = workOrder;
    }

    @Override
    public void fromBytesOverride(final FriendlyByteBuf buf)
    {
        workOrder = buf.readInt();
    }

    @Override
    public void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeInt(workOrder);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingBuilder building)
    {
        building.setWorkOrder(workOrder);
    }
}
