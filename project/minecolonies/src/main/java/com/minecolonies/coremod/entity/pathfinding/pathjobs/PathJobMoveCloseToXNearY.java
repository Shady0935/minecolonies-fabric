package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.coremod.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
    private final Vec3 initialFeetPosition;

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
        this.initialFeetPosition = entity.position();
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
        // Reaching the start cell does not move the entity to that cell's center.
        // Use the captured real position for a zero-movement route, too.
        final boolean inReach = node.parent == null
          ? isWithinWorkRange(initialFeetPosition, desiredPosition, eyeHeight, distanceToDesired)
          : isWithinWorkRange(node.pos, desiredPosition, eyeHeight, distanceToDesired);
        return inReach
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(below), below, getPathingOptions()) == SurfaceType.WALKABLE;
    }

    /** Shared by route selection and the Builder so a reachable endpoint stays usable. */
    public static boolean isWithinWorkRange(final BlockPos standingPosition, final BlockPos target,
                                             final double eyeHeight, final int range)
    {
        return isWithinWorkRange(Vec3.atBottomCenterOf(standingPosition), target, eyeHeight, range);
    }

    /** Use real feet coordinates for the live worker, and node centers for planned positions. */
    public static boolean isWithinWorkRange(final Vec3 standingPosition, final BlockPos target,
                                             final double eyeHeight, final int range)
    {
        final BlockPos standingBlock = BlockPos.containing(standingPosition);
        final int blockDx = standingBlock.getX() - target.getX();
        final int blockDz = standingBlock.getZ() - target.getZ();
        if (Math.abs(blockDx) + Math.abs(blockDz) > range
              || (blockDx == 0 && blockDz == 0 && standingPosition.y >= target.getY()))
        {
            return false;
        }

        // A block is worked on at its nearest face, not only at its center.
        // In particular, the lower face of a roof can be inside five blocks
        // while its center is outside. Keep the same bounded interaction range.
        final double dx = distanceToBlockAxis(standingPosition.x, target.getX());
        final double dy = distanceToBlockAxis(standingPosition.y + eyeHeight, target.getY());
        final double dz = distanceToBlockAxis(standingPosition.z, target.getZ());
        return dx * dx + dy * dy + dz * dz <= (double) range * range;
    }

    private static double distanceToBlockAxis(final double coordinate, final int blockCoordinate)
    {
        return Math.max(0.0D, Math.max(blockCoordinate - coordinate, coordinate - (blockCoordinate + 1.0D)));
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
