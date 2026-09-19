package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.network.NetworkEvent;

/**
 * Message to set the colony default structure style.
 */
public class ColonyStructureStyleMessage extends AbstractColonyServerMessage
{
    /**
     * The chosen pack.
     */
    private String pack;

    /**
     * Default constructor
     **/
    public ColonyStructureStyleMessage()
    {
        super();
    }

    /**
     * Change the colony default pack from the client to the serverside.
     *
     * @param colony the colony the player changed the pack in.
     * @param pack   the pack name,
     */
    public ColonyStructureStyleMessage(final IColony colony, final String pack)
    {
        super(colony);
        this.pack = pack;
    }

    /**
     * Creates a server-bound structure-style message without requiring a client colony view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     * @param pack selected structure pack
     */
    public ColonyStructureStyleMessage(final ResourceKey<Level> dimensionId, final int colonyId, final String pack)
    {
        super(dimensionId, colonyId);
        this.pack = pack;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.setStructurePack(pack);
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
        buf.writeUtf(pack);
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
        this.pack = buf.readUtf(32767);
    }
}

