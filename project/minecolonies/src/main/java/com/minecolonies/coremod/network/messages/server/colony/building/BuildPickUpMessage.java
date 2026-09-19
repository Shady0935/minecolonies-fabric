package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Picks up the building block with the level.
 */
public class BuildPickUpMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * Empty constructor used when registering the
     */
    public BuildPickUpMessage()
    {
        super();
    }

    /**
     * Creates a build request
     *
     * @param building the building we're executing on.
     */
    public BuildPickUpMessage(@NotNull final IBuildingView building)
    {
        super(building);
    }

    /** Creates a server-bound request without requiring a client building view. */
    public BuildPickUpMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId)
    {
        super(dimensionId, colonyId, buildingId);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final Player player = ctxIn.getSender();
        building.pickUp(player);
    }
}
