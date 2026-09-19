package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.event.ColonyInformationChangedEvent;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to execute the renaiming of the townHall.
 */
public class TownHallRenameMessage extends AbstractColonyServerMessage
{
    private static final int    MAX_NAME_LENGTH  = 25;
    private static final int    SUBSTRING_LENGTH = MAX_NAME_LENGTH - 1;
    private              String name;

    /**
     * Empty public constructor.
     */
    public TownHallRenameMessage()
    {
        super();
    }

    /**
     * Object creation for the town hall rename
     *
     * @param colony Colony the rename is going to occur in.
     * @param name   New name of the town hall.
     */
    public TownHallRenameMessage(@NotNull final IColonyView colony, final String name)
    {
        super(colony);
        this.name = normalizeName(name);
    }

    /**
     * Creates a server-bound rename message without requiring a client colony view.
     *
     * @param dimensionId colony dimension
     * @param colonyId colony id
     * @param name requested new name
     */
    public TownHallRenameMessage(final ResourceKey<Level> dimensionId, final int colonyId, final String name)
    {
        super(dimensionId, colonyId);
        this.name = normalizeName(name);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        name = buf.readUtf(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeUtf(name);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        name = normalizeName(name);
        colony.setName(name);

        if (ctxIn.getSender() != null)
        {
            Network.getNetwork().sendToEveryone(this);
        }

        if (isLogicalServer)
        {
            MinecraftForge.EVENT_BUS.post(new ColonyInformationChangedEvent(colony, ColonyInformationChangedEvent.Type.NAME));
        }
    }

    private static String normalizeName(final String name)
    {
        return (name.length() <= MAX_NAME_LENGTH) ? name : name.substring(0, SUBSTRING_LENGTH);
    }
}
