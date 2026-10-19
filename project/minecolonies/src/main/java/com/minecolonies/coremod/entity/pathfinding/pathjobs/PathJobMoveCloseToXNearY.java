package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.coremod.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Finds a safe work position close to a target while preferring positions near a second point.
 */
public class PathJobMoveCloseToXNearY extends AbstractPathJob
{
    private final BlockPos desiredPosition;
    private final BlockPos nearbyPosition;
    private final int distanceToDesired;
    private final double eyeHeight;

    public PathJobMoveCloseToXNearY(final Level world,
                                    @NotNull final BlockPos start,
                                    @NotNull final BlockPos desiredPosition,
                                    @NotNull final BlockPos nearbyPosition,
                                    final int distanceToDesired,
                                    final int searchRange,
                                    @NotNull final LivingEntity entity)
    {
        super(world, start, desiredPosition, searchRange, new PathResult<PathJobMoveCloseToXNearY>(), entity);
        this.desiredPosition = desiredPosition;
        this.nearbyPosition = nearbyPosition;
        this.distanceToDesired = distanceToDesired;
        this.eyeHeight = entity.getEyeHeight();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        return manhattanDistance(desiredPosition, pos) + manhattanDistance(nearbyPosition, pos) * 2.0D;
    }

    @Override
    protected boolean isAtDestination(@NotNull final MNode node)
    {
        final BlockPos below = node.pos.below();
        return isWithinWorkRange(node.pos, desiredPosition, eyeHeight, distanceToDesired)
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(below), below, getPathingOptions()) == SurfaceType.WALKABLE;
    }

    /** Shared by route selection and the Builder so a reachable endpoint stays usable. */
    public static boolean isWithinWorkRange(final BlockPos standingPosition, final BlockPos target,
                                             final double eyeHeight, final int range)
    {
        final double dx = standingPosition.getX() - target.getX();
        final double dz = standingPosition.getZ() - target.getZ();
        if (Math.abs(dx) + Math.abs(dz) > range
              || (dx == 0 && dz == 0 && standingPosition.getY() >= target.getY()))
        {
            return false;
        }

        // Measure reach from the worker's eyes to the target block's center,
        // rather than rejecting an upper block by its height above the feet.
        final double dy = standingPosition.getY() + eyeHeight - (target.getY() + 0.5D);
        return dx * dx + dy * dy + dz * dz <= (double) range * range;
    }

    @Override
    protected double getNodeResultScore(@NotNull final MNode node)
    {
        if (node.pos.getX() == desiredPosition.getX() && node.pos.getZ() == desiredPosition.getZ()
              && node.pos.getY() >= desiredPosition.getY())
        {
            return 1000.0D;
        }

        double score = manhattanDistance(desiredPosition, node.pos) * 2.0D + manhattanDistance(nearbyPosition, node.pos);
        if (node.isSwimming() || SurfaceType.isWater(world, node.pos.below()))
        {
            score += 50.0D;
        }
        return score;
    }

    private static int manhattanDistance(@NotNull final BlockPos first, @NotNull final BlockPos second)
    {
        return Math.abs(first.getX() - second.getX())
                 + Math.abs(first.getY() - second.getY())
                 + Math.abs(first.getZ() - second.getZ());
    }
}
