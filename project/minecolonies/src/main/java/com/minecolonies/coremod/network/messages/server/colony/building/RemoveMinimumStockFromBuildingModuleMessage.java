package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IMinimumStockModule;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Set a new block to the minimum stock list.
 */
public class RemoveMinimumStockFromBuildingModuleMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The module's id
     */
    private int moduleId;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;

    /**
     * Empty constructor used when registering the
     */
    public RemoveMinimumStockFromBuildingModuleMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param building  the building we're executing on.
     * @param itemStack to be take from the player for the building
     */
    public RemoveMinimumStockFromBuildingModuleMessage(final IBuildingView building, final ItemStack itemStack, final int moduleId)
    {
        super(building);
        this.itemStack = itemStack;
        this.moduleId = moduleId;
    }

    /**
     * Creates a server-bound minimum-stock removal from stable world identifiers.
     *
     * @param dimensionId the dimension containing the building
     * @param colonyId    the colony owning the building
     * @param buildingId  the building position
     * @param itemStack   the item to remove from minimum stock
     * @param moduleId    the module runtime id
     */
    public RemoveMinimumStockFromBuildingModuleMessage(final ResourceKey<Level> dimensionId, final int colonyId,
      final BlockPos buildingId, final ItemStack itemStack, final int moduleId)
    {
        super(dimensionId, colonyId, buildingId);
        this.itemStack = itemStack;
        this.moduleId = moduleId;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        itemStack = buf.readItem();
        moduleId = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(itemStack);
        buf.writeInt(moduleId);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.getModule(moduleId) instanceof IMinimumStockModule module)
        {
            module.removeMinimumStock(itemStack);
        }
    }
}
