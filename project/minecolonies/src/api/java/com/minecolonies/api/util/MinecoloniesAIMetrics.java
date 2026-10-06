package com.minecolonies.api.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Opt-in, low-overhead counters for profiling citizen AI and navigation.
 * Enable with {@code -Dminecolonies.ai.metrics=true} on the game JVM.
 */
public final class MinecoloniesAIMetrics
{
    private static final boolean ENABLED = Boolean.getBoolean("minecolonies.ai.metrics");
    private static final long REPORT_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(60);
    private static final Object REPORT_LOCK = new Object();

    private static final LongAdder AI_TICKS = new LongAdder();
    private static final LongAdder AI_NANOS = new LongAdder();
    private static final AtomicLong MAX_AI_NANOS = new AtomicLong();
    private static final LongAdder PATH_JOBS_SUBMITTED = new LongAdder();
    private static final LongAdder PATH_JOBS_COMPLETED = new LongAdder();
    private static final LongAdder PATH_JOBS_CANCELLED = new LongAdder();
    private static final LongAdder REPATHS = new LongAdder();
    private static final LongAdder PATH_NANOS = new LongAdder();
    private static final AtomicLong MAX_PATH_NANOS = new AtomicLong();
    private static final LongAdder PATH_NODES_VISITED = new LongAdder();
    private static final LongAdder PATH_NODES_ADDED = new LongAdder();
    private static final LongAdder PATH_NODES_IN_RESULT = new LongAdder();
    private static final LongAdder STUCK_EVENTS = new LongAdder();
    private static final LongAdder STUCK_RECOVERIES = new LongAdder();
    private static final LongAdder WALK_DISTANCE_MILLIBLOCKS = new LongAdder();
    private static final LongAdder DELIVERY_DISTANCE_MILLIBLOCKS = new LongAdder();
    private static final LongAdder BUILDER_DISTANCE_MILLIBLOCKS = new LongAdder();
    private static final LongAdder WALKING_TICKS = new LongAdder();
    private static final LongAdder IDLE_TICKS = new LongAdder();
    private static final LongAdder WORK_TICKS = new LongAdder();
    private static final LongAdder BUILDER_WALKING_TICKS = new LongAdder();
    private static final LongAdder BUILDER_WORK_TICKS = new LongAdder();
    private static final LongAdder STATE_CHANGES = new LongAdder();
    private static final LongAdder TARGET_CHANGES = new LongAdder();
    private static final LongAdder BUILDER_ACTIONS = new LongAdder();
    private static final LongAdder BUILDER_PLACEMENT_STEPS = new LongAdder();
    private static final LongAdder BUILDER_BLOCKS_PLACED = new LongAdder();
    private static final LongAdder BUILDER_BLOCKS_BROKEN = new LongAdder();
    private static final LongAdder BUILDER_WORK_FROM_CHANGES = new LongAdder();
    private static final LongAdder DELIVERIES = new LongAdder();
    private static final LongAdder ITEMS_DELIVERED = new LongAdder();
    private static final LongAdder WAREHOUSE_RETURNS = new LongAdder();
    private static final LongAdder DELIVERY_DESTINATIONS = new LongAdder();
    private static final LongAdder GROUPED_DELIVERIES = new LongAdder();

    private static volatile long nextReportNanos = System.nanoTime() + REPORT_INTERVAL_NANOS;

    private MinecoloniesAIMetrics()
    {
    }

    public static boolean isEnabled()
    {
        return ENABLED;
    }

    public static void recordAITick(final long elapsedNanos)
    {
        if (!ENABLED)
        {
            return;
        }
        AI_TICKS.increment();
        AI_NANOS.add(elapsedNanos);
        MAX_AI_NANOS.accumulateAndGet(elapsedNanos, Math::max);
    }

    public static void recordPathJobStarted(final boolean repath)
    {
        if (!ENABLED)
        {
            return;
        }
        PATH_JOBS_SUBMITTED.increment();
        if (repath)
        {
            REPATHS.increment();
        }
    }

    public static void recordPathJobCancelled()
    {
        if (ENABLED)
        {
            PATH_JOBS_CANCELLED.increment();
        }
    }

    public static void recordPathJobCompleted(final long elapsedNanos, final int nodesVisited, final int nodesAdded, final int pathNodes)
    {
        if (!ENABLED)
        {
            return;
        }
        PATH_JOBS_COMPLETED.increment();
        PATH_NANOS.add(elapsedNanos);
        MAX_PATH_NANOS.accumulateAndGet(elapsedNanos, Math::max);
        PATH_NODES_VISITED.add(nodesVisited);
        PATH_NODES_ADDED.add(nodesAdded);
        PATH_NODES_IN_RESULT.add(pathNodes);
    }

    public static void recordStuckEvent()
    {
        if (ENABLED)
        {
            STUCK_EVENTS.increment();
        }
    }

    public static void recordStuckRecovery()
    {
        if (ENABLED)
        {
            STUCK_RECOVERIES.increment();
        }
    }

    public static void recordCitizenActivity(final double distance,
                                             final boolean walking,
                                             final boolean idle,
                                             final boolean working,
                                             final boolean deliveryRoute,
                                             final boolean builder)
    {
        if (!ENABLED)
        {
            return;
        }
        if (distance > 0 && Double.isFinite(distance))
        {
            final long milliblocks = Math.round(distance * 1000.0D);
            WALK_DISTANCE_MILLIBLOCKS.add(milliblocks);
            if (deliveryRoute)
            {
                DELIVERY_DISTANCE_MILLIBLOCKS.add(milliblocks);
            }
            if (builder)
            {
                BUILDER_DISTANCE_MILLIBLOCKS.add(milliblocks);
            }
        }
        if (walking)
        {
            WALKING_TICKS.increment();
        }
        if (idle)
        {
            IDLE_TICKS.increment();
        }
        if (working)
        {
            WORK_TICKS.increment();
        }
        if (builder)
        {
            if (walking)
            {
                BUILDER_WALKING_TICKS.increment();
            }
            else if (working)
            {
                BUILDER_WORK_TICKS.increment();
            }
        }
    }

    public static void recordStateChange()
    {
        if (ENABLED)
        {
            STATE_CHANGES.increment();
        }
    }

    public static void recordTargetChange()
    {
        if (ENABLED)
        {
            TARGET_CHANGES.increment();
        }
    }

    public static void recordBuilderAction()
    {
        if (ENABLED)
        {
            BUILDER_ACTIONS.increment();
        }
    }

    public static void recordBuilderPlacementStep()
    {
        if (ENABLED)
        {
            BUILDER_PLACEMENT_STEPS.increment();
        }
    }

    public static void recordBuilderBlocksPlaced(final int count)
    {
        if (ENABLED && count > 0)
        {
            BUILDER_BLOCKS_PLACED.add(count);
        }
    }

    public static void recordBuilderBlockBroken()
    {
        if (ENABLED)
        {
            BUILDER_BLOCKS_BROKEN.increment();
        }
    }

    public static void recordBuilderBlocksBroken(final int count)
    {
        if (ENABLED && count > 0)
        {
            BUILDER_BLOCKS_BROKEN.add(count);
        }
    }

    public static void recordBuilderWorkFromChange()
    {
        if (ENABLED)
        {
            BUILDER_WORK_FROM_CHANGES.increment();
        }
    }

    public static void recordWarehouseReturn()
    {
        if (ENABLED)
        {
            WAREHOUSE_RETURNS.increment();
        }
    }

    public static void recordDeliveryDestination()
    {
        if (ENABLED)
        {
            DELIVERY_DESTINATIONS.increment();
        }
    }

    public static void recordDelivery(final int itemCount, final boolean grouped)
    {
        if (!ENABLED)
        {
            return;
        }
        DELIVERIES.increment();
        ITEMS_DELIVERED.add(itemCount);
        if (grouped)
        {
            GROUPED_DELIVERIES.increment();
        }
    }

    public static void reportIfDue()
    {
        if (!ENABLED || System.nanoTime() < nextReportNanos)
        {
            return;
        }

        synchronized (REPORT_LOCK)
        {
            final long now = System.nanoTime();
            if (now < nextReportNanos)
            {
                return;
            }
            nextReportNanos = now + REPORT_INTERVAL_NANOS;

            final long aiTicks = AI_TICKS.sumThenReset();
            final long aiNanos = AI_NANOS.sumThenReset();
            final long maxAiNanos = MAX_AI_NANOS.getAndSet(0);
            final long pathJobs = PATH_JOBS_SUBMITTED.sumThenReset();
            final long completedPaths = PATH_JOBS_COMPLETED.sumThenReset();
            final long cancelledPaths = PATH_JOBS_CANCELLED.sumThenReset();
            final long pathNanos = PATH_NANOS.sumThenReset();
            final long maxPathNanos = MAX_PATH_NANOS.getAndSet(0);
            final long nodesVisited = PATH_NODES_VISITED.sumThenReset();
            final long nodesAdded = PATH_NODES_ADDED.sumThenReset();
            final long pathNodes = PATH_NODES_IN_RESULT.sumThenReset();
            final long repaths = REPATHS.sumThenReset();
            final long stuckEvents = STUCK_EVENTS.sumThenReset();
            final long stuckRecoveries = STUCK_RECOVERIES.sumThenReset();
            final long distance = WALK_DISTANCE_MILLIBLOCKS.sumThenReset();
            final long deliveryDistance = DELIVERY_DISTANCE_MILLIBLOCKS.sumThenReset();
            final long builderDistance = BUILDER_DISTANCE_MILLIBLOCKS.sumThenReset();
            final long walkingTicks = WALKING_TICKS.sumThenReset();
            final long idleTicks = IDLE_TICKS.sumThenReset();
            final long workTicks = WORK_TICKS.sumThenReset();
            final long builderWalkingTicks = BUILDER_WALKING_TICKS.sumThenReset();
            final long builderWorkTicks = BUILDER_WORK_TICKS.sumThenReset();
            final long stateChanges = STATE_CHANGES.sumThenReset();
            final long targetChanges = TARGET_CHANGES.sumThenReset();
            final long builderActions = BUILDER_ACTIONS.sumThenReset();
            final long builderPlacements = BUILDER_PLACEMENT_STEPS.sumThenReset();
            final long builderBlocksPlaced = BUILDER_BLOCKS_PLACED.sumThenReset();
            final long builderBlocksBroken = BUILDER_BLOCKS_BROKEN.sumThenReset();
            final long builderWorkFromChanges = BUILDER_WORK_FROM_CHANGES.sumThenReset();
            final long deliveries = DELIVERIES.sumThenReset();
            final long itemsDelivered = ITEMS_DELIVERED.sumThenReset();
            final long warehouseReturns = WAREHOUSE_RETURNS.sumThenReset();
            final long destinations = DELIVERY_DESTINATIONS.sumThenReset();
            final long groupedDeliveries = GROUPED_DELIVERIES.sumThenReset();

            Log.getLogger().info("AI metrics (last 60s): aiTicks=" + aiTicks
                                   + ", avgAiMs=" + averageMillis(aiNanos, aiTicks)
                                   + ", maxAiMs=" + nanosToMillis(maxAiNanos)
                                   + ", pathJobs=" + pathJobs
                                   + ", completedPaths=" + completedPaths
                                   + ", cancelledPaths=" + cancelledPaths
                                   + ", repaths=" + repaths
                                   + ", avgPathMs=" + averageMillis(pathNanos, completedPaths)
                                   + ", maxPathMs=" + nanosToMillis(maxPathNanos)
                                   + ", pathNodesVisited=" + nodesVisited
                                   + ", pathNodesAdded=" + nodesAdded
                                   + ", pathNodesInResults=" + pathNodes
                                   + ", stuckEvents=" + stuckEvents
                                   + ", stuckRecoveries=" + stuckRecoveries
                                   + ", distance=" + milliblocksToBlocks(distance)
                                   + ", walkingTicks=" + walkingTicks
                                   + ", idleTicks=" + idleTicks
                                   + ", workTicks=" + workTicks
                                   + ", builderDistance=" + milliblocksToBlocks(builderDistance)
                                   + ", builderWalkingTicks=" + builderWalkingTicks
                                   + ", builderWorkTicks=" + builderWorkTicks
                                   + ", stateChanges=" + stateChanges
                                   + ", targetChanges=" + targetChanges
                                   + ", builderActions=" + builderActions
                                   + ", builderActionsPerMinute=" + builderActions
                                   + ", builderPlacementSteps=" + builderPlacements
                                   + ", builderBlocksPlaced=" + builderBlocksPlaced
                                   + ", builderDistancePerAction=" + (builderActions == 0 ? 0 : milliblocksToBlocks(builderDistance) / builderActions)
                                   + ", builderBlocksBroken=" + builderBlocksBroken
                                   + ", builderWorkFromChanges=" + builderWorkFromChanges
                                   + ", deliveries=" + deliveries
                                   + ", itemsDelivered=" + itemsDelivered
                                   + ", warehouseReturns=" + warehouseReturns
                                   + ", destinations=" + destinations
                                   + ", groupedDeliveries=" + groupedDeliveries
                                   + ", deliveryDistance=" + milliblocksToBlocks(deliveryDistance)
                                   + ", distancePerDelivery=" + (deliveries == 0 ? 0 : milliblocksToBlocks(deliveryDistance) / deliveries));
        }
    }

    private static double averageMillis(final long nanos, final long samples)
    {
        return samples == 0 ? 0.0D : nanosToMillis(nanos) / samples;
    }

    private static double nanosToMillis(final long nanos)
    {
        return nanos / 1_000_000.0D;
    }

    private static double milliblocksToBlocks(final long milliblocks)
    {
        return milliblocks / 1000.0D;
    }
}
