package com.ldtteam.multipiston;

import com.ldtteam.structurize.api.util.IRotatableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.UP;

/** Server-ticked state and movement implementation for the Multi-Piston. */
public final class TileEntityMultiPiston extends BlockEntity implements IRotatableBlockEntity
{
    public static final String TAG_INPUT = "input";
    public static final String TAG_RANGE = "range";
    public static final String TAG_DIRECTION = "direction";
    public static final String TAG_LENGTH = "length";
    public static final String TAG_PROGRESS = "progress";
    public static final String TAG_OUTPUT_DIRECTION = "outputDirection";
    public static final String TAG_SPEED = "speed";
    private static final String TAG_CURRENT_DIRECTION = "currentDirection";
    private static final String TAG_TICKS_PASSED = "ticksPassed";

    public static final double VOLUME = 0.5D;
    public static final double PITCH = 0.8D;
    public static final int MAX_RANGE = 10;
    public static final int MAX_SPEED = 3;
    public static final int MIN_SPEED = 1;
    public static final int DEFAULT_RANGE = 3;
    public static final int DEFAULT_SPEED = 2;

    private boolean on;
    private Direction input = UP;
    private Direction output = DOWN;
    private int range = DEFAULT_RANGE;
    @Nullable private Direction currentDirection;
    private int progress;
    private int ticksPassed;
    private int speed = DEFAULT_SPEED;

    public TileEntityMultiPiston(final BlockPos pos, final BlockState state)
    {
        super(ModTileEntities.MULTIPISTON, pos, state);
    }

    /** Begin a stroke when the redstone state changes and the previous stroke is complete. */
    public void handleRedstone(final boolean signal)
    {
        if (signal == on || progress != range)
        {
            return;
        }
        on = signal;
        currentDirection = signal ? output : input;
        progress = 0;
        ticksPassed = 0;
        setChanged();
    }

    /** Tick the actuator on the logical server. */
    public void tick()
    {
        if (level == null || level.isClientSide)
        {
            return;
        }
        if (currentDirection == null && progress < range)
        {
            progress = range;
            setChanged();
        }
        if (progress >= range || currentDirection == null)
        {
            return;
        }

        if (ticksPassed % (20 / speed) == 0)
        {
            handleTick();
            ticksPassed = 1;
        }
        ticksPassed++;
    }

    /** Move matching blocks one step between the configured input and output faces. */
    public void handleTick()
    {
        if (level == null || currentDirection == null || progress >= range)
        {
            return;
        }

        final Direction oppositeFace = currentDirection == input ? output : input;
        final BlockState blockToMove = level.getBlockState(worldPosition.relative(currentDirection));
        final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockToMove.getBlock());
        if (blockToMove.isAir()
              || blockToMove.getPistonPushReaction() == PushReaction.IGNORE
              || blockToMove.getPistonPushReaction() == PushReaction.DESTROY
              || blockToMove.getPistonPushReaction() == PushReaction.BLOCK
              || blockToMove.getBlock() instanceof EntityBlock
                && (blockId == null || !"domum_ornamentum".equals(blockId.getNamespace()))
              || blockToMove.getBlock() == Blocks.BEDROCK)
        {
            progress++;
            setChanged();
            return;
        }

        for (int index = 0; index < Math.min(range, MAX_RANGE); index++)
        {
            final int targetCandidate = index - 1 - progress;
            final int sourceCandidate = index + 1 - progress;
            final int targetOffset = targetCandidate + (targetCandidate >= 0 ? 1 : 0);
            final int sourceOffset = sourceCandidate - (sourceCandidate <= 0 ? 1 : 0);
            final BlockPos targetPos = targetOffset > 0
              ? worldPosition.relative(currentDirection, targetOffset)
              : worldPosition.relative(oppositeFace, Math.abs(targetOffset));
            final BlockPos sourcePos = sourceOffset > 0
              ? worldPosition.relative(currentDirection, sourceOffset)
              : worldPosition.relative(oppositeFace, Math.abs(sourceOffset));

            if (!level.hasChunkAt(targetPos) || !level.hasChunkAt(sourcePos)
                  || !level.isEmptyBlock(targetPos) && !level.getBlockState(targetPos).liquid())
            {
                continue;
            }

            BlockState stateToMove = level.getBlockState(sourcePos);
            if (blockToMove.getBlock() != stateToMove.getBlock())
            {
                continue;
            }

            pushEntitiesIfNecessary(targetPos, worldPosition);
            stateToMove = Block.updateFromNeighbourShapes(stateToMove, level, targetPos);
            if (!level.setBlock(targetPos, stateToMove, 67))
            {
                continue;
            }
            if (stateToMove.getBlock() instanceof BucketPickup bucketPickup)
            {
                bucketPickup.pickupBlock(level, targetPos, stateToMove);
            }
            level.neighborChanged(targetPos, stateToMove.getBlock(), targetPos);

            if (stateToMove.getBlock() instanceof EntityBlock)
            {
                final BlockEntity sourceEntity = level.getBlockEntity(sourcePos);
                final BlockEntity targetEntity = level.getBlockEntity(targetPos);
                if (sourceEntity != null && targetEntity != null)
                {
                    final CompoundTag blockEntityData = sourceEntity.saveWithId();
                    targetEntity.load(blockEntityData);
                    targetEntity.setChanged();
                }
            }
            level.removeBlock(sourcePos, false);
        }

        level.playSound(null, worldPosition, SoundEvents.PISTON_EXTEND,
          SoundSource.BLOCKS, (float) VOLUME, (float) PITCH);
        progress++;
        setChanged();
    }

    private void pushEntitiesIfNecessary(final BlockPos destination, final BlockPos origin)
    {
        final List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(destination));
        final BlockPos vector = destination.subtract(origin);
        final BlockPos shifted = destination.relative(Direction.getNearest(vector.getX(), vector.getY(), vector.getZ()));
        for (final Entity entity : entities)
        {
            entity.teleportTo(shifted.getX() + 0.5D, shifted.getY() + 0.5D, shifted.getZ() + 0.5D);
        }
    }

    @Override
    public void rotate(final Rotation rotation)
    {
        if (output != UP && output != DOWN)
        {
            output = rotation.rotate(output);
        }
        if (input != UP && input != DOWN)
        {
            input = rotation.rotate(input);
        }
        setChanged();
    }

    @Override
    public void mirror(final Mirror mirror)
    {
        if (output != UP && output != DOWN)
        {
            output = mirror.mirror(output);
        }
        if (input != UP && input != DOWN)
        {
            input = mirror.mirror(input);
        }
        setChanged();
    }

    public boolean isOn()
    {
        return on;
    }

    public Direction getInput()
    {
        return input;
    }

    public Direction getOutput()
    {
        return output;
    }

    public int getRange()
    {
        return range;
    }

    public int getSpeed()
    {
        return speed;
    }

    public int getProgress()
    {
        return progress;
    }

    @Nullable
    public Direction getCurrentDirection()
    {
        return currentDirection;
    }

    public void setInput(final Direction direction)
    {
        input = direction;
        setChanged();
    }

    public void setOutput(final Direction direction)
    {
        output = direction;
        setChanged();
    }

    public void setRange(final int requestedRange)
    {
        range = Mth.clamp(requestedRange, 1, MAX_RANGE);
        progress = range;
        currentDirection = null;
        ticksPassed = 0;
        setChanged();
    }

    public void setSpeed(final int requestedSpeed)
    {
        speed = Mth.clamp(requestedSpeed, MIN_SPEED, MAX_SPEED);
        setChanged();
    }

    public void setConfiguration(final Direction input, final Direction output, final int range, final int speed)
    {
        this.input = input;
        this.output = output;
        this.range = Mth.clamp(range, 1, MAX_RANGE);
        this.speed = Mth.clamp(speed, MIN_SPEED, MAX_SPEED);
        progress = this.range;
        currentDirection = null;
        ticksPassed = 0;
        setChanged();
    }

    @Override
    public void load(@NotNull final CompoundTag tag)
    {
        super.load(tag);
        range = Mth.clamp(tag.contains(TAG_RANGE) ? tag.getInt(TAG_RANGE) : DEFAULT_RANGE, 1, MAX_RANGE);
        progress = Mth.clamp(tag.getInt(TAG_PROGRESS), 0, range);
        input = directionFromTag(tag, TAG_DIRECTION, UP);
        output = directionFromTag(tag, TAG_OUTPUT_DIRECTION, input.getOpposite());
        on = tag.getBoolean(TAG_INPUT);
        speed = Mth.clamp(tag.contains(TAG_SPEED) ? tag.getInt(TAG_SPEED) : DEFAULT_SPEED, MIN_SPEED, MAX_SPEED);
        currentDirection = tag.contains(TAG_CURRENT_DIRECTION)
          ? directionFromTag(tag, TAG_CURRENT_DIRECTION, null) : null;
        ticksPassed = Math.max(0, tag.getInt(TAG_TICKS_PASSED));
    }

    @Nullable
    private static Direction directionFromTag(final CompoundTag tag, final String key, @Nullable final Direction fallback)
    {
        if (!tag.contains(key))
        {
            return fallback;
        }
        final int index = tag.getInt(key);
        return index >= 0 && index < Direction.values().length ? Direction.values()[index] : fallback;
    }

    @Override
    protected void saveAdditional(@NotNull final CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt(TAG_RANGE, range);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_DIRECTION, input.ordinal());
        tag.putBoolean(TAG_INPUT, on);
        tag.putInt(TAG_OUTPUT_DIRECTION, output.ordinal());
        tag.putInt(TAG_SPEED, speed);
        tag.putInt(TAG_CURRENT_DIRECTION, currentDirection == null ? -1 : currentDirection.ordinal());
        tag.putInt(TAG_TICKS_PASSED, ticksPassed);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithId();
    }
}
