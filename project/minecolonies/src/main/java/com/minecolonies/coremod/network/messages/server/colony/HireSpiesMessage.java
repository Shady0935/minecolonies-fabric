package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.inventory.InvWrapper;
import com.minecolonies.fabric.network.NetworkEvent;

import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBarracks.SPIES_GOLD_COST;

/**
 * Message for hiring spies at the cost of gold.
 */
public class HireSpiesMessage extends AbstractColonyServerMessage
{
    public HireSpiesMessage()
    {
    }

    public HireSpiesMessage(final IColony colony)
    {
        super(colony);
    }

    /**
     * Creates a server-bound message without requiring a client colony view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     */
    public HireSpiesMessage(final ResourceKey<Level> dimensionId, final int colonyId)
    {
        super(dimensionId, colonyId);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        if (InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.getInventory()), stack -> stack.getItem() == Items.GOLD_INGOT) >= SPIES_GOLD_COST)
        {
            InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.getInventory()), new ItemStack(Items.GOLD_INGOT), SPIES_GOLD_COST);
            colony.getRaiderManager().setSpiesEnabled(true);
            colony.markDirty();
        }
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {


    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {

    }
}
