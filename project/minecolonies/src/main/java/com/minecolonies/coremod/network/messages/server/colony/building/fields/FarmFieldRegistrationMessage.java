package com.minecolonies.coremod.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.coremod.colony.fields.FarmField;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;

import java.util.Optional;

/**
 * TODO: Remove in 1.20.2
 */
public class FarmFieldRegistrationMessage extends AbstractColonyServerMessage
{
    /**
     * The field position.
     */
    private BlockPos position;

    /**
     * Forge default constructor
     */
    public FarmFieldRegistrationMessage()
    {
        super();
    }

    /**
     * @param position the field position.
     */
    public FarmFieldRegistrationMessage(IColony colony, BlockPos position)
    {
        super(colony);
        this.position = position;
    }

    /**
     * Creates a server-bound field registration message without requiring a client colony view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     * @param position field position
     */
    public FarmFieldRegistrationMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos position)
    {
        super(dimensionId, colonyId);
        this.position = position;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer || ctxIn.getSender() == null)
        {
            return;
        }

        final Optional<IField> field = colony.getBuildingManager()
                                         .getField(f -> f.getFieldType().equals(FieldRegistries.farmField.get()) && f.getPosition().equals(position))
                                         .stream()
                                         .findFirst();

        if (field.isEmpty())
        {
            colony.getBuildingManager().addField(FarmField.create(position));
        }
    }

    @Override
    public void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(position);
    }

    @Override
    public void fromBytesOverride(final FriendlyByteBuf buf)
    {
        position = buf.readBlockPos();
    }
}
