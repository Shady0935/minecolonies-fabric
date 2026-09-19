package com.minecolonies.coremod.network.messages.server.colony.citizen;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import com.minecolonies.fabric.network.NetworkEvent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_CITIZEN_RECALL_FAILED;

/**
 * Recalls the citizen to the location.
 */
public class RecallSingleCitizenMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The citizen id.
     */
    private int citizenId;

    /**
     * Empty public constructor.
     */
    public RecallSingleCitizenMessage()
    {
        super();
    }

    /**
     * Object creation for the recall.
     *
     * @param building  View of the building the citizen should be teleported to.
     * @param citizenid the id of the citizen.
     */
    public RecallSingleCitizenMessage(final IBuildingView building, final int citizenid)
    {
        super(building);
        this.citizenId = citizenid;
    }

    /**
     * Creates a server-bound recall request without requiring a client-side building view.
     *
     * @param dimensionId the colony dimension.
     * @param colonyId    the colony id.
     * @param buildingId  the target building position.
     * @param citizenId   the citizen to recall.
     */
    public RecallSingleCitizenMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId,
      final int citizenId)
    {
        super(dimensionId, colonyId, buildingId);
        this.citizenId = citizenId;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        citizenId = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        buf.writeInt(citizenId);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        citizenData.setLastPosition(building.getPosition());
        Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
        if (!optionalEntityCitizen.isPresent())
        {
            citizenData.updateEntityIfNecessary();
            optionalEntityCitizen = citizenData.getEntity();
        }

        if (optionalEntityCitizen.isPresent() && optionalEntityCitizen.get().getTicksExisted() == 0)
        {
            citizenData.updateEntityIfNecessary();
        }

        final BlockPos loc = building.getID();
        if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
        {
            final Player player = ctxIn.getSender();
            if (player == null)
            {
                return;
            }

            MessageUtils.format(WARNING_CITIZEN_RECALL_FAILED).sendTo(player);
        }
    }
}
