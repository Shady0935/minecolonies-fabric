package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;

/**
 * Message to set the colony texture style.
 */
public class ColonyTextureStyleMessage extends AbstractColonyServerMessage
{
    /**
     * The chosen style.
     */
    private String style;

    /**
     * Default constructor
     **/
    public ColonyTextureStyleMessage()
    {
        super();
    }

    /**
     * Change the colony style from the client to the serverside.
     *
     * @param colony      the colony the player changed the style in.
     * @param style the list of patterns they set in the banner picker
     */
    public ColonyTextureStyleMessage(final IColony colony, final String style)
    {
        super(colony);
        this.style = style;
    }

    /**
     * Creates a server-bound texture-style message without requiring a client colony view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     * @param style selected texture style
     */
    public ColonyTextureStyleMessage(final ResourceKey<Level> dimensionId, final int colonyId, final String style)
    {
        super(dimensionId, colonyId);
        this.style = style;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.setTextureStyle(style);
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
        buf.writeUtf(style);
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
        this.style = buf.readUtf(32767);
    }
}
