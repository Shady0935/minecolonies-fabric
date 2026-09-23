package com.ldtteam.multipiston.network;

import com.ldtteam.multipiston.MultiPiston;
import com.ldtteam.multipiston.TileEntityMultiPiston;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Validated block configuration sent from the BlockUI window to the server. */
public record MultiPistonChangeMessage(BlockPos pos, Direction input, Direction output, int range, int speed)
{
    public static final ResourceLocation ID = MultiPiston.id("net-channel");

    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(input.get3DDataValue());
        buffer.writeVarInt(output.get3DDataValue());
        buffer.writeVarInt(range);
        buffer.writeVarInt(speed);
    }

    public static MultiPistonChangeMessage read(final FriendlyByteBuf buffer)
    {
        final BlockPos pos = buffer.readBlockPos();
        final int inputId = buffer.readVarInt();
        final int outputId = buffer.readVarInt();
        final int range = buffer.readVarInt();
        final int speed = buffer.readVarInt();
        if (inputId < 0 || inputId >= Direction.values().length
              || outputId < 0 || outputId >= Direction.values().length)
        {
            throw new IllegalArgumentException("Invalid Multi-Piston direction id");
        }
        return new MultiPistonChangeMessage(pos, Direction.from3DDataValue(inputId),
          Direction.from3DDataValue(outputId), range, speed);
    }

    public boolean apply(final ServerPlayer player)
    {
        if (player == null || input == output || range < 1 || range > TileEntityMultiPiston.MAX_RANGE
              || speed < TileEntityMultiPiston.MIN_SPEED || speed > TileEntityMultiPiston.MAX_SPEED)
        {
            return false;
        }

        final Level level = player.level();
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D
              || !level.mayInteract(player, pos))
        {
            return false;
        }

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TileEntityMultiPiston multiPiston))
        {
            return false;
        }

        multiPiston.setConfiguration(input, output, range, speed);
        final BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
        return true;
    }
}
