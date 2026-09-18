package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the server telling nearby clients to render a particle effect. Created: February 10, 2016
 *
 * @author Colton
 */
public class BlockParticleEffectMessage implements IMessage
{
    public static final int BREAK_BLOCK = -1;

    private BlockPos   pos;
    private BlockState block;
    private int        side;

    /**
     * Empty constructor used when registering the
     */
    public BlockParticleEffectMessage()
    {
        super();
    }

    /**
     * Sends a message for particle effect.
     *
     * @param pos   Coordinates
     * @param state Block State
     * @param side  Side of the block causing effect
     */
    public BlockParticleEffectMessage(final BlockPos pos, @NotNull final BlockState state, final int side)
    {
        this.pos = pos;
        this.block = state;
        this.side = side;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        block = Block.stateById(buf.readInt());
        side = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(Block.getId(block));
        buf.writeInt(side);
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
        ClientMessageBridge.invoke("handleBlockParticleEffectMessage",
          new Class<?>[] {BlockPos.class, BlockState.class, int.class}, pos, block, side);
    }
}
