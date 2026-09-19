package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of items to filterable item lists.
 */
public class AssignFilterableItemMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    /**
     * True if assign, false if remove.
     */
    private boolean assign;

    /**
     * The item in question.
     */
    private ItemStorage item;

    /**
     * The id of the list.
     */
    private int id;

    /**
     * Empty standard constructor.
     */
    public AssignFilterableItemMessage()
    {
        super();
    }

    /**
     * Creates the message to add an item.
     *
     * @param id       the id of the list of filterables.
     * @param assign   compost if true, dont if false.
     * @param item     the item to assign
     * @param building the building we're executing on.
     */
    public AssignFilterableItemMessage(final IBuildingView building, final int id, final ItemStorage item, final boolean assign)
    {
        super(building);
        this.assign = assign;
        this.item = item;
        this.id = id;
    }

    /**
     * Creates a server-bound message without requiring a client-side building view.
     *
     * @param dimensionId the colony dimension.
     * @param colonyId    the colony id.
     * @param buildingId  the building position.
     * @param id          the id of the list of filterables.
     * @param item        the item to assign or remove.
     * @param assign      true to assign, false to remove.
     */
    public AssignFilterableItemMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId,
      final int id, final ItemStorage item, final boolean assign)
    {
        super(dimensionId, colonyId, buildingId);
        this.assign = assign;
        this.item = item;
        this.id = id;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        this.assign = buf.readBoolean();
        this.item = new ItemStorage(buf.readItem());
        this.id = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        buf.writeBoolean(this.assign);
        buf.writeItem(this.item.getItemStack());
        buf.writeInt(this.id);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuilding building)
    {
        if (building.getModule(id) instanceof ItemListModule module)
        {
            if (assign)
            {
                module.addItem(item);
            }
            else
            {
                module.removeItem(item);
            }
        }
    }
}

