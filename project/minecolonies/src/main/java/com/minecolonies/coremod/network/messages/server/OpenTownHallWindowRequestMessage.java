package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.client.OpenTownHallWindowMessage;
import com.minecolonies.coremod.network.messages.client.UpdateChunkCapabilityMessage;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewBuildingViewMessage;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewMessage;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;
import static com.minecolonies.api.blocks.ModBlocks.blockHutTownHall;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.TOOL_PERMISSION_SCEPTER_PERMISSION_DENY;

/** Server-validated request to open the correct Town Hall screen. */
public class OpenTownHallWindowRequestMessage implements IMessage
{
    private BlockPos pos;
    private boolean openInventory;

    public OpenTownHallWindowRequestMessage()
    {
    }

    public OpenTownHallWindowRequestMessage(final BlockPos pos, final boolean openInventory)
    {
        this.pos = pos;
        this.openInventory = openInventory;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        openInventory = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeBoolean(openInventory);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context context, final boolean isLogicalServer)
    {
        final ServerPlayer player = context.getSender();
        if (player == null || player.blockPosition().distSqr(pos) > 64 || !player.level().hasChunkAt(pos)
              || !player.level().getBlockState(pos).is(blockHutTownHall))
        {
            return;
        }

        final Level world = player.level();
        final IColonyManager manager = IColonyManager.getInstance();
        final IBuilding building = manager.getBuilding(world, pos);
        final boolean hasRegisteredTownHall = building != null;
        if (hasRegisteredTownHall && !building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            MessageUtils.format(TOOL_PERMISSION_SCEPTER_PERMISSION_DENY).sendTo(player);
            return;
        }

        final Set<IColony> coloniesToRefresh = new LinkedHashSet<>();
        if (hasRegisteredTownHall)
        {
            coloniesToRefresh.add(building.getColony());
        }
        final IColony colonyAtPosition = manager.getIColony(world, pos);
        final IColony ownerColony = manager.getIColonyByOwner(world, player);
        final IColony closestColony = manager.getClosestColony(world, pos);
        if (colonyAtPosition != null)
        {
            coloniesToRefresh.add(colonyAtPosition);
        }
        if (ownerColony != null)
        {
            coloniesToRefresh.add(ownerColony);
        }
        if (closestColony != null)
        {
            coloniesToRefresh.add(closestColony);
        }

        for (final IColony colony : coloniesToRefresh)
        {
            if (colony instanceof Colony concreteColony)
            {
                final FriendlyByteBuf viewData = new FriendlyByteBuf(Unpooled.buffer());
                final ColonyViewMessage viewMessage;
                try
                {
                    ColonyView.serializeNetworkData(concreteColony, viewData, false);
                    viewMessage = new ColonyViewMessage(colony.getID(), colony.getDimension(), viewData);
                }
                finally
                {
                    viewData.release();
                }
                viewMessage.setIsNewSubscription(false);
                Network.getNetwork().sendToPlayer(viewMessage, player);
                Network.getNetwork().sendToPlayer(new PermissionsMessage.View(concreteColony,
                  concreteColony.getPermissions().getRank(player)), player);
            }
        }

        final LevelChunk chunk = world.getChunkAt(pos);
        com.minecolonies.fabric.capability.CapabilityHooks.getCapability(chunk, CLOSE_COLONY_CAP, null).ifPresent(capability ->
          Network.getNetwork().sendToPlayer(new UpdateChunkCapabilityMessage(capability, chunk.getPos().x, chunk.getPos().z), player));

        if (hasRegisteredTownHall)
        {
            Network.getNetwork().sendToPlayer(new ColonyViewBuildingViewMessage(building), player);
        }
        Network.getNetwork().sendToPlayer(new OpenTownHallWindowMessage(pos, hasRegisteredTownHall, openInventory), player);
    }
}
