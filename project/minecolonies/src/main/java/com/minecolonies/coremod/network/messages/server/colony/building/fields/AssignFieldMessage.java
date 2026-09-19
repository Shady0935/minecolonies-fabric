package com.minecolonies.coremod.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.colony.fields.registry.FieldDataManager;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of fields to farmers.
 */
public class AssignFieldMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The modules ID
     */
    private int       moduleID = 0;

    /**
     * The field to (un)assign.
     */
    private FriendlyByteBuf fieldData;

    /**
     * Whether to assign or un-assign this field.
     */
    private boolean assign;

    /**
     * Empty standard constructor.
     */
    public AssignFieldMessage()
    {
        super();
    }

    /**
     * Creates the message to assign a field.
     *
     * @param assign   assign if true, free if false.
     * @param field    the field.
     * @param building the building we're executing on.
     */
    public AssignFieldMessage(final IBuildingView building, final IField field, final boolean assign, final int moduleID)
    {
        super(building);
        this.assign = assign;
        this.fieldData = FieldDataManager.fieldToBuffer(field);
        this.moduleID = moduleID;
    }

    /**
     * Creates a server-bound field assignment message without requiring a client building view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     * @param buildingId target building position
     * @param field the field to assign or free
     * @param assign assign if true, free if false
     * @param moduleID field-module runtime id
     */
    public AssignFieldMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos buildingId,
      final IField field, final boolean assign, final int moduleID)
    {
        super(dimensionId, colonyId, buildingId);
        this.assign = assign;
        this.fieldData = FieldDataManager.fieldToBuffer(field);
        this.moduleID = moduleID;
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        fieldData.resetReaderIndex();
        buf.writeBoolean(assign);
        buf.writeInt(moduleID);
        buf.writeBytes(fieldData);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        assign = buf.readBoolean();
        moduleID = buf.readInt();
        fieldData = new FriendlyByteBuf(Unpooled.buffer(buf.readableBytes()));
        buf.readBytes(fieldData, buf.readableBytes());
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final IField parsedField = FieldDataManager.bufferToField(fieldData);
        colony.getBuildingManager().getField(otherField -> otherField.equals(parsedField)).ifPresent(field -> {

            if (building.getModule(moduleID) instanceof FieldsModule fieldsModule)
            {
                if (assign)
                {
                    fieldsModule.assignField(field);
                }
                else
                {
                    fieldsModule.freeField(field);
                }
            }
        });
    }
}

