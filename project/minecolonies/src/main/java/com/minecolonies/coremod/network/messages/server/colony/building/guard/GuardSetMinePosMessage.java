package com.minecolonies.coremod.network.messages.server.colony.building.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the position of the mine a guard should patrol
 */
public class GuardSetMinePosMessage extends AbstractBuildingServerMessage<AbstractBuildingGuards>
{
    /**
     * the position of the mine (can be null)
     */
    private BlockPos minePos;
    /**
     * Indicates whether minePos is a valid position
     */
    private Boolean hasMinePos = false;

    /**
     * Empty standard constructor
     */
    public GuardSetMinePosMessage()
    {
        super();
    }

    /**
     * Creates an instance of the message to set a new position
     * @param building the building to apply the position change to
     * @param minePos the position of the mine
     */
    public GuardSetMinePosMessage(@NotNull AbstractBuildingGuards.View building, BlockPos minePos)
    {
        super(building);
        this.minePos = minePos;
        this.hasMinePos = true;
    }

    /**
     * Creates an instance of the message to clear the position
     * @param building the building to apply the position change to
     */
    public GuardSetMinePosMessage(@NotNull AbstractBuildingGuards.View building)
    {
        super(building);
    }

    /**
     * Creates a server-bound message to select a mine without requiring a client building view.
     *
     * @param dimensionId colony dimension
     * @param colonyId    colony id
     * @param buildingId  guard-tower position
     * @param minePos     miner position to patrol
     */
    public GuardSetMinePosMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId,
      final BlockPos minePos)
    {
        super(dimensionId, colonyId, buildingId);
        this.minePos = minePos;
        this.hasMinePos = true;
    }

    /**
     * Creates a server-bound message to clear the guard's assigned mine.
     *
     * @param dimensionId colony dimension
     * @param colonyId    colony id
     * @param buildingId  guard-tower position
     */
    public GuardSetMinePosMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId)
    {
        super(dimensionId, colonyId, buildingId);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.hasMinePos = buf.readBoolean();
        if (this.hasMinePos)
        {
            this.minePos = buf.readBlockPos();
        }
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(this.hasMinePos);
        if (this.hasMinePos)
        {
            buf.writeBlockPos(this.minePos);
        }
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuildingGuards building)
    {
        if (!this.hasMinePos)
        {
            building.setMinePos(null);
            return;
        }

        final IBuilding miner = building.getColony().getBuildingManager().getBuilding(this.minePos);
        if (miner instanceof BuildingMiner)
        {
            building.setMinePos(this.minePos);
        }
    }
}
