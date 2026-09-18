package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Handles the server causing compost particle effects.
 */
public class CompostParticleMessage implements IMessage
{
    /**
     * Random obj for values.
     */
    public static final java.util.Random random = new java.util.Random();

    /**
     * The position.
     */
    private BlockPos pos;

    /**
     * Empty constructor used when registering the
     */
    public CompostParticleMessage()
    {
        super();
    }

    /**
     * Sends a message for particle effect.
     *
     * @param pos Coordinates
     */
    public CompostParticleMessage(final BlockPos pos)
    {
        super();
        this.pos = pos;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
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
        ClientMessageBridge.invoke("handleCompostParticleMessage",
          new Class<?>[] {BlockPos.class}, pos);
    }
}
