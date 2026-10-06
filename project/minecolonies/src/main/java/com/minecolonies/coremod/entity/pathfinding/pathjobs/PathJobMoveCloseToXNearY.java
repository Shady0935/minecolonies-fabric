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
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        return manhattanDistance(desiredPosition, pos) + manhattanDistance(nearbyPosition, pos) * 2.0D;
    }

    @Override
    protected boolean isAtDestination(@NotNull final MNode node)
    {
        if (node.pos.getX() == desiredPosition.getX() && node.pos.getZ() == desiredPosition.getZ())
        {
            return false;
        }

        final BlockPos below = node.pos.below();
        return manhattanDistance(desiredPosition, node.pos) <= distanceToDesired
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(below), below, getPathingOptions()) == SurfaceType.WALKABLE;
    }

    @Override
    protected double getNodeResultScore(@NotNull final MNode node)
    {
        if (node.pos.getX() == desiredPosition.getX() && node.pos.getZ() == desiredPosition.getZ())
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
