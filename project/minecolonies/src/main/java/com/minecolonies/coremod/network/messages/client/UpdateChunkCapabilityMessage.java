package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.util.ChunkCapData;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minecolonies.fabric.network.ClientMessageBridge;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkCapabilityMessage implements IMessage
{
    /**
     * The chunk cap data
     */
    private ChunkCapData chunkCapData;

    /**
     * Empty constructor used when registering the
     */
    public UpdateChunkCapabilityMessage()
    {
        super();
    }

    /**
     * Create a message to update the chunk cap on the client side.
     *
     * @param tagCapability the cap.
     * @param x             the x pos.
     * @param z             the z pos.
     */
    public UpdateChunkCapabilityMessage(@NotNull final IColonyTagCapability tagCapability, final int x, final int z)
    {
        chunkCapData = new ChunkCapData(x, z, tagCapability.getOwningColony(), tagCapability.getStaticClaimColonies());
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        chunkCapData = ChunkCapData.fromBytes(buf);
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        chunkCapData.toBytes(buf);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        ClientMessageBridge.invoke("handleUpdateChunkCapabilityMessage",
          new Class<?>[] {ChunkCapData.class}, chunkCapData);
    }
}
