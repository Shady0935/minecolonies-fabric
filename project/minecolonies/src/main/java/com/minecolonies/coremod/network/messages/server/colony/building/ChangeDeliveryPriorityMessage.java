package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.Stash;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class ChangeDeliveryPriorityMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * If up true, if down false.
     */
    private boolean up;

    /**
     * Empty public constructor.
     */
    public ChangeDeliveryPriorityMessage()
    {
        super();
    }

    /**
     * Creates message for player to change the priority of the delivery.
     *
     * @param building view of the building to read data from
     * @param up       up or down?
     */
    public ChangeDeliveryPriorityMessage(@NotNull final IBuildingView building, final boolean up)
    {
        super(building);
        this.up = up;
    }

    /**
     * Creates a server-bound priority message without requiring a client building view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     * @param buildingId target building position
     * @param up whether to increase or decrease the priority
     */
    public ChangeDeliveryPriorityMessage(final ResourceKey<Level> dimensionId, final int colonyId,
      final BlockPos buildingId, final boolean up)
    {
        super(dimensionId, colonyId, buildingId);
        this.up = up;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.up = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(this.up);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if ((building != null && building.hasModule(WorkerBuildingModule.class)) || building instanceof Stash)
        {
            if (up)
            {
                building.alterPickUpPriority(1);
            }
            else
            {
                building.alterPickUpPriority(-1);
            }
            building.markDirty();
        }
    }
}
