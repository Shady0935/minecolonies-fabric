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
 * Message class which manages the message to toggle the help messages.
 */
public class ToggleHelpMessage extends AbstractColonyServerMessage
{
    /**
     * Empty public constructor.
     */
    public ToggleHelpMessage()
    {
        super();
    }

    /**
     * Creates object for the player to turn help messages or or off.
     *
     * @param colony view of the colony to read data from.
     */
    public ToggleHelpMessage(@NotNull final IColonyView colony)
    {
        super(colony);
    }

    /**
     * Creates a server-bound message without requiring a client colony view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     */
    public ToggleHelpMessage(final ResourceKey<Level> dimensionId, final int colonyId)
    {
        super(dimensionId, colonyId);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        colony.getProgressManager().togglePrintProgress();
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
