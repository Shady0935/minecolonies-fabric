package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.moduleviews.ItemListModuleView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the reset of items to filterable item lists.
 */
public class ResetFilterableItemMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    /**
     * The id of the list.
     */
    private int id;

    /**
     * Empty standard constructor.
     */
    public ResetFilterableItemMessage()
    {
        super();
    }

    /**
     * Creates the message to reset a list..
     *
     * @param id       the id of the list of filterables.
     * @param building the building we're executing on.
     */
    public ResetFilterableItemMessage(final IBuildingView building, final int id)
    {
        super(building);
        this.id = id;
    }

    /**
     * Creates a server-bound message without requiring a client-side building view.
     *
     * @param dimensionId the colony dimension.
     * @param colonyId    the colony id.
     * @param buildingId  the building position.
     * @param id          the id of the list of filterables.
     */
    public ResetFilterableItemMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId,
      final int id)
    {
        super(dimensionId, colonyId, buildingId);
        this.id = id;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.id = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(this.id);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuilding building)
    {
        if (building.getModule(id) instanceof ItemListModule module)
        {
            module.resetToDefaults();
        }
    }
}
