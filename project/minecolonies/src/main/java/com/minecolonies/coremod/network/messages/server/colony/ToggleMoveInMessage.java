package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the message to toggle automatic or manual housing allocation.
 */
public class ToggleMoveInMessage extends AbstractColonyServerMessage
{
    /**
     * Toggle the housing allocation to true or false.
     */
    private boolean toggle;

    /**
     * Empty public constructor.
     */
    public ToggleMoveInMessage()
    {
        super();
    }

    /**
     * Creates object for the player to turn manual housing allocation or or off.
     *
     * @param colony view of the colony to read data from.
     * @param toggle toggle the housing to manually or automatically.
     */
    public ToggleMoveInMessage(@NotNull final IColonyView colony, final boolean toggle)
    {
        super(colony);
        this.toggle = toggle;
    }

    /**
     * Creates a server-bound message without requiring a client colony view.
     *
     * @param dimensionId colony dimension
     * @param colonyId    colony id
     * @param toggle      whether citizens may move in
     */
    public ToggleMoveInMessage(final ResourceKey<Level> dimensionId, final int colonyId, final boolean toggle)
    {
        super(dimensionId, colonyId);
        this.toggle = toggle;
    }

    /**
     * Transformation from a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        toggle = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        buf.writeBoolean(toggle);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        colony.setMoveIn(toggle);
    }
}
