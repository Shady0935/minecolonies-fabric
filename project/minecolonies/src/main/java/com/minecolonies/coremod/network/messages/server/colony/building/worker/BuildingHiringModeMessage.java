package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IAssignsCitizen;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the hiring mode of a building.
 */
public class BuildingHiringModeMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The Hiring mode to set.
     */
    private HiringMode    mode;

    /**
     * The job id.
     */
    private int moduleId;

    /**
     * Empty constructor used when registering the
     */
    public BuildingHiringModeMessage()
    {
        super();
    }

    /**
     * Creates object for the hiring mode
     *
     * @param building View of the building to read data from.
     * @param mode     the hiring mode.
     */
    public BuildingHiringModeMessage(@NotNull final IBuildingView building, final HiringMode mode, final int moduleId)
    {
        super(building);
        this.mode = mode;
        this.moduleId = moduleId;
    }

    /**
     * Creates a server-bound message without requiring a client building view.
     *
     * @param dimensionId colony dimension
     * @param colonyId    colony id
     * @param buildingId  target building position
     * @param mode        hiring mode to select
     * @param moduleId    assigned-citizen module runtime id
     */
    public BuildingHiringModeMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId,
      final HiringMode mode, final int moduleId)
    {
        super(dimensionId, colonyId, buildingId);
        this.mode = mode;
        this.moduleId = moduleId;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        mode = HiringMode.values()[buf.readInt()];
        moduleId = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(mode.ordinal());
        buf.writeInt(moduleId);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.getModule(moduleId) instanceof IAssignsCitizen module)
        {
            module.setHiringMode(mode);
        }
    }
}
