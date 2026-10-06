package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.placement.AbstractBlueprintIterator;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.pathfinding.PathingOptions;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MinecoloniesAIMetrics;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.modules.settings.BuilderModeSetting;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuilding;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.AbstractPathJob;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobMoveCloseToXNearY;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.STANDARD_WORKING_RANGE;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * AI class for the builder. Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructureWithWorkOrder<JobBuilder, BuildingBuilder>
{
    /**
     * Speed buff at 0 depth level.
     */
    private static final double SPEED_BUFF_0 = 0.5;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 4096;

    /**
     * Building level to purge mobs at the build site.
     */
    private static final int LEVEL_TO_PURGE_MOBS = 4;

    /** Maximum future work operations retained by the local planner. */
    private static final int WORK_LOOKAHEAD_TARGET_LIMIT = 32;

    /** Maximum raw blueprint positions inspected to fill the bounded work window. */
    private static final int WORK_LOOKAHEAD_SCAN_LIMIT = 512;

    /**
     * Path used to select the next safe work position.
     */
    private PathResult<AbstractPathJob> workPositionPath;

    /** Cached world positions of upcoming operations in the current Structurize stage. */
    private final List<BlockPos> plannedWorkTargets = new ArrayList<>();

    /** Stage and progress cursor that produced the cached target window. */
    private BuildingStructureHandler.Stage plannedWorkStage;
    private BlockPos plannedProgressCursor;
    private BlockPos plannedWorkOrderPosition;

    /** Best future operation cluster used as a secondary pathing preference. */
    private BlockPos plannedRouteTarget;

    /** Last planner invalidation cause, kept for local diagnostics and profiling. */
    private String plannerInvalidationReason = "uninitialized";

    private boolean workPlanValid;

    /** Whether the preview iterator reached the end of the active stage. */
    private boolean workPlanStageExhausted;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 100),
          new AITarget(START_WORKING, this::checkForWorkOrder, this::startWorkingAtOwnBuilding, 100)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public int getBreakSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    @Override
    public int getPlaceSpeedLevel()
    {
        return getPrimarySkillLevel();
    }

    @Override
    public Class<BuildingBuilder> getExpectedBuildingClass()
    {
        return BuildingBuilder.class;
    }

    /**
     * Checks if we got a valid workorder.
     *
     * @return true if we got a workorder to work with
     */
    private boolean checkForWorkOrder()
    {
        if (!job.hasWorkOrder())
        {
            building.searchWorkOrder();
            building.setProgressPos(null, BuildingStructureHandler.Stage.CLEAR);
            return false;
        }

        final IWorkOrder wo = job.getWorkOrder();

        if (wo == null)
        {
            job.setWorkOrder(null);
            building.setProgressPos(null, null);
            return false;
        }

        final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getLocation());
        if (building == null && wo instanceof WorkOrderBuilding && wo.getWorkOrderType() != WorkOrderType.REMOVE)
        {
            job.complete();
            return false;
        }

        return true;
    }

    @Override
    public void setStructurePlacer(final BuildingStructureHandler<JobBuilder, BuildingBuilder> structure)
    {
        if (job.getWorkOrder().getIteratorType().isEmpty())
        {
            final String mode = BuilderModeSetting.getActualValue(building);
            job.getWorkOrder().setIteratorType(mode);
        }

        final StructurePlacer placer = new StructurePlacer(structure, job.getWorkOrder().getIteratorType());
        placer.setTrackBlockChanges(MinecoloniesAIMetrics.isEnabled());
        structurePlacer = new Tuple<>(placer, structure);
        resetWorkPlanner("structure_changed", true);
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return !checkForWorkOrder();
    }

    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return LOAD_STRUCTURE;
    }

    /**
     * Kill all mobs at the building site.
     */
    private void killMobs()
    {
        if (building.getBuildingLevel() >= LEVEL_TO_PURGE_MOBS && job.getWorkOrder().getWorkOrderType() == WorkOrderType.BUILD)
        {
            final BlockPos buildingPos = job.getWorkOrder().getLocation();
            final IBuilding building = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(buildingPos);
            if (building != null)
            {
                WorldUtil.getEntitiesWithinBuilding(world, Monster.class, building, null).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
            }
        }
    }

    @Override
    public void checkForExtraBuildingActions()
    {
        if (!building.hasPurgedMobsToday())
        {
            killMobs();
            building.setPurgedMobsToday(true);
        }
    }

    @Override
    protected boolean mineBlock(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        return mineBlock(blockToMine, safeStand, true, !IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(blockToMine)), null);
    }

    @Override
    public IAIState afterRequestPickUp()
    {
        return INVENTORY_FULL;
    }

    @Override
    public IAIState afterDump()
    {
        return PICK_UP;
    }

    @Override
    public boolean walkToConstructionSite(final BlockPos currentBlock)
    {
        final IWorkOrder workOrder = job.getWorkOrder();
        final BlockPos nearbyPosition = workOrder == null ? building.getPosition() : workOrder.getLocation();

        final boolean miningObstruction = blockToMine != null;
        if (miningObstruction)
        {
            if (workPlanValid || !plannedWorkTargets.isEmpty())
            {
                resetWorkPlanner("required_mining", true);
            }
        }
        else
        {
            updateWorkPlan(nearbyPosition);
        }

        final BlockPos nextTarget = !miningObstruction && !plannedWorkTargets.isEmpty()
                                      ? plannedWorkTargets.get(0)
                                      : currentBlock;

        if (!miningObstruction && plannedWorkTargets.isEmpty() && workPlanStageExhausted)
        {
            if (workPositionPath != null)
            {
                workPositionPath.cancel();
                workPositionPath = null;
            }
            if (worker.getNavigation().isInProgress())
            {
                worker.getNavigation().stop();
            }
            return true;
        }

        if (workFrom != null
              && workFrom.getX() == nextTarget.getX()
              && workFrom.getZ() == nextTarget.getZ()
              && workFrom.getY() >= nextTarget.getY())
        {
            updateWorkFrom(null);
        }

        if (!plannedWorkTargets.isEmpty()
              && isWorkTargetReachableFrom(worker.blockPosition(), nextTarget)
              && isWorkPositionSafe(worker.blockPosition()))
        {
            if (workPositionPath != null)
            {
                workPositionPath.cancel();
                workPositionPath = null;
            }
            if (worker.getNavigation().isInProgress())
            {
                worker.getNavigation().stop();
            }
            if (workFrom == null || !isWorkTargetReachableFrom(workFrom, nextTarget) || !isWorkPositionSafe(workFrom))
            {
                updateWorkFrom(worker.blockPosition());
            }
            plannedRouteTarget = choosePlannerAnchor(worker.blockPosition(), nearbyPosition);
            MinecoloniesAIMetrics.recordBuilderPlannerInPlace(countCoveredTargets(worker.blockPosition()));
            return true;
        }

        if (!plannedWorkTargets.isEmpty() && workFrom != null && !isWorkTargetReachableFrom(workFrom, nextTarget))
        {
            updateWorkFrom(null);
        }

        if (workFrom == null)
        {
            if (workPositionPath == null || workPositionPath.isCancelled())
            {
                final BlockPos routePreference = !plannedWorkTargets.isEmpty()
                                                   ? choosePlannerAnchor(worker.blockPosition(), nearbyPosition)
                                                   : nearbyPosition;
                plannedRouteTarget = routePreference;
                final PathJobMoveCloseToXNearY pathJob = new PathJobMoveCloseToXNearY(
                  world,
                  AbstractPathJob.prepareStart(worker),
                  nextTarget,
                  routePreference,
                  4,
                  (int) worker.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
                  worker);
                workPositionPath = ((MinecoloniesAdvancedPathNavigate) worker.getNavigation()).setPathJob(
                  pathJob,
                  nextTarget,
                  1.0D,
                  false,
                  options -> options.setCanDrop(false));
            }
            else if (workPositionPath.isDone())
            {
                final Path path = workPositionPath.getPath();
                if (path != null && (plannedWorkTargets.isEmpty() || isWorkTargetReachableFrom(path.getTarget(), nextTarget)))
                {
                    updateWorkFrom(path.getTarget());
                }
                else if (path == null)
                {
                    resetWorkPlanner("path_failed", false);
                }
                else
                {
                    updateWorkFrom(null);
                    plannedRouteTarget = null;
                    plannerInvalidationReason = "route_outside_work_range";
                }
                workPositionPath = null;
            }

            return false;
        }

        if (!plannedWorkTargets.isEmpty()
              && !isWorkTargetReachableFrom(worker.blockPosition(), nextTarget)
              && worker.getNavigation().isInProgress())
        {
            return false;
        }

        if (!worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE))
        {
            if (worker.getNavigation() instanceof MinecoloniesAdvancedPathNavigate pathNavigate && pathNavigate.isPathingStuck())
            {
                resetWorkPlanner("stuck", true);
            }
            return false;
        }

        if (!isWorkPositionSafe(workFrom))
        {
            resetWorkPlanner("work_position_unsafe", true);
            return false;
        }

        if (!plannedWorkTargets.isEmpty() && !isWorkTargetReachableFrom(worker.blockPosition(), nextTarget))
        {
            updateWorkFrom(null);
            return false;
        }

        if (plannedWorkTargets.isEmpty() && BlockPosUtil.getDistance2D(worker.blockPosition(), currentBlock) > STANDARD_WORKING_RANGE)
        {
            updateWorkFrom(null);
            return false;
        }

        if (!plannedWorkTargets.isEmpty())
        {
            MinecoloniesAIMetrics.recordBuilderPlannerInPlace(countCoveredTargets(worker.blockPosition()));
        }
        return true;
    }

    /**
     * Rebuilds the bounded operation window only when the structure stage or progress cursor changes.
     */
    private void updateWorkPlan(final BlockPos workOrderPosition)
    {
        if (structurePlacer == null || !structurePlacer.getB().hasBluePrint())
        {
            return;
        }

        final BuildingStructureHandler.Stage stage = structurePlacer.getB().getStage();
        final Tuple<BlockPos, BuildingStructureHandler.Stage> progress = getProgressPos();
        final BlockPos cursor = progress == null ? AbstractBlueprintIterator.NULL_POS : progress.getA();
        final boolean siteChanged = plannedWorkOrderPosition != null && plannedWorkOrderPosition.distSqr(workOrderPosition) > 25;

        if (!workPlanValid || plannedWorkStage != stage || siteChanged)
        {
            resetWorkPlanner(plannedWorkStage != stage ? "stage_changed" : siteChanged ? "work_area_changed" : "new_plan", siteChanged);
            plannedWorkStage = stage;
            plannedWorkOrderPosition = workOrderPosition;
            plannedProgressCursor = cursor;
            collectUpcomingWorkTargets(stage, cursor);
            workPlanValid = true;
            MinecoloniesAIMetrics.recordBuilderPlannerWindow(plannedWorkTargets.size());
            return;
        }

        if (cursor.equals(plannedProgressCursor))
        {
            if (!plannedWorkTargets.isEmpty() && !isCurrentPlanTargetStillRelevant(stage))
            {
                resetWorkPlanner("next_target_changed", true);
                plannedWorkStage = stage;
                plannedWorkOrderPosition = workOrderPosition;
                plannedProgressCursor = cursor;
                collectUpcomingWorkTargets(stage, cursor);
                workPlanValid = true;
                MinecoloniesAIMetrics.recordBuilderPlannerWindow(plannedWorkTargets.size());
            }
            return;
        }

        final BlockPos worldCursor = structurePlacer.getB().getProgressPosInWorld(cursor);
        final int completedTargetIndex = plannedWorkTargets.indexOf(worldCursor);
        if (completedTargetIndex < 0)
        {
            resetWorkPlanner("cursor_outside_window", true);
            plannedWorkStage = stage;
            plannedWorkOrderPosition = workOrderPosition;
            plannedProgressCursor = cursor;
            collectUpcomingWorkTargets(stage, cursor);
            workPlanValid = true;
            MinecoloniesAIMetrics.recordBuilderPlannerWindow(plannedWorkTargets.size());
            return;
        }

        plannedWorkTargets.subList(0, completedTargetIndex + 1).clear();
        plannedProgressCursor = cursor;
        if (plannedWorkTargets.isEmpty())
        {
            collectUpcomingWorkTargets(stage, cursor);
            MinecoloniesAIMetrics.recordBuilderPlannerWindow(plannedWorkTargets.size());
        }
    }

    /**
     * Samples the real iterator order without advancing the live StructurePlacer iterator.
     */
    private void collectUpcomingWorkTargets(final BuildingStructureHandler.Stage stage, final BlockPos cursor)
    {
        plannedWorkTargets.clear();
        workPlanStageExhausted = false;
        if (stage == null)
        {
            return;
        }

        final boolean includeEntities = stage == BuildingStructureHandler.Stage.SPAWN || stage == BuildingStructureHandler.Stage.REMOVE;
        final boolean removing = stage == BuildingStructureHandler.Stage.REMOVE;
        final AbstractBlueprintIterator preview = structurePlacer.getA().createIteratorPreview(cursor, includeEntities, removing);
        final boolean forward = stage == BuildingStructureHandler.Stage.BUILD_SOLID
                                  || stage == BuildingStructureHandler.Stage.WEAK_SOLID
                                  || stage == BuildingStructureHandler.Stage.DECORATE
                                  || stage == BuildingStructureHandler.Stage.SPAWN;

        for (int scanned = 0; scanned < WORK_LOOKAHEAD_SCAN_LIMIT && plannedWorkTargets.size() < WORK_LOOKAHEAD_TARGET_LIMIT; scanned++)
        {
            final AbstractBlueprintIterator.Result result = forward ? preview.increment() : preview.decrement();
            if (result == AbstractBlueprintIterator.Result.AT_END)
            {
                workPlanStageExhausted = true;
                break;
            }
            if (result != AbstractBlueprintIterator.Result.NEW_BLOCK)
            {
                break;
            }

            final BlockPos localPos = preview.getProgressPos();
            final BlueprintPositionInfo info = preview.getBluePrintPositionInfo(localPos);
            if (info == null || info.getBlockInfo() == null)
            {
                continue;
            }

            final BlockPos worldPos = structurePlacer.getB().getProgressPosInWorld(localPos);
            if (world.isOutsideBuildHeight(worldPos))
            {
                continue;
            }
            if (!isUpcomingWorkOperation(stage, info, worldPos, includeEntities, removing))
            {
                continue;
            }
            plannedWorkTargets.add(worldPos.immutable());
        }
    }

    /**
     * Mirrors the Builder's stage filters for planning, without executing a placement or success callback.
     */
    private boolean isUpcomingWorkOperation(final BuildingStructureHandler.Stage stage,
                                            final BlueprintPositionInfo info,
                                            final BlockPos worldPos,
                                            final boolean includeEntities,
                                            final boolean removing)
    {
        final com.ldtteam.structurize.placement.structure.IStructureHandler handler = structurePlacer.getB();
        final BlockState blueprintState = info.getBlockInfo().getState();
        final BlockState worldState = world.getBlockState(worldPos);
        final Block blueprintBlock = blueprintState.getBlock();

        switch (stage)
        {
            case BUILD_SOLID:
                if (DONT_TOUCH_PREDICATE.test(info, worldPos, handler)
                      || !BlockUtils.canBlockFloatInAir(blueprintState)
                      || isDecoItem(blueprintBlock))
                {
                    return false;
                }
                break;
            case WEAK_SOLID:
                if (DONT_TOUCH_PREDICATE.test(info, worldPos, handler) || !BlockUtils.isWeakSolidBlock(blueprintState))
                {
                    return false;
                }
                break;
            case CLEAR_WATER:
                if (worldState.getFluidState().isEmpty())
                {
                    return false;
                }
                break;
            case CLEAR_NON_SOLIDS:
                if (DONT_TOUCH_PREDICATE.test(info, worldPos, handler) || !(blueprintBlock instanceof AirBlock) || world.isEmptyBlock(worldPos))
                {
                    return false;
                }
                break;
            case DECORATE:
                if (DONT_TOUCH_PREDICATE.test(info, worldPos, handler)
                      || (BlockUtils.isAnySolid(blueprintState) && !isDecoItem(blueprintBlock)))
                {
                    return false;
                }
                break;
            case SPAWN:
                if (DONT_TOUCH_PREDICATE.test(info, worldPos, handler) || info.getEntities().length == 0)
                {
                    return false;
                }
                break;
            case REMOVE_WATER:
                if (blueprintState.getFluidState().isEmpty())
                {
                    return false;
                }
                break;
            case REMOVE:
                if (worldState.getBlock() instanceof AirBlock
                      || blueprintBlock instanceof AirBlock
                      || !worldState.getFluidState().isEmpty()
                      || blueprintBlock == ModBlocks.blockSolidSubstitution.get()
                      || blueprintBlock == ModBlocks.blockSubstitution.get()
                      || worldState.getBlock() instanceof IBuilderUndestroyable)
                {
                    return false;
                }
                break;
            case CLEAR:
            default:
                if (worldState.getBlock() instanceof IBuilderUndestroyable
                      || worldState.getBlock() == Blocks.BEDROCK
                      || worldState.getBlock() instanceof AirBlock
                      || blueprintBlock == ModBlocks.blockFluidSubstitution.get()
                      || !worldState.getFluidState().isEmpty())
                {
                    return false;
                }
                break;
        }

        if (!removing
              && BlockUtils.areBlockStatesEqual(blueprintState,
                   worldState,
                   handler::replaceWithSolidBlock,
                   handler.fancyPlacement(),
                   handler::shouldBlocksBeConsideredEqual,
                   info.getBlockInfo().getTileEntityData(),
                   info.getBlockInfo().getTileEntityData() == null ? null : world.getBlockEntity(worldPos))
              && (!includeEntities || info.getEntities().length == 0))
        {
            return false;
        }
        return true;
    }

    /**
     * Selects the most useful future work cluster while preferring a short route from the Builder.
     */
    private BlockPos choosePlannerAnchor(final BlockPos currentPosition, final BlockPos workOrderPosition)
    {
        if (plannedWorkTargets.size() < 2)
        {
            return workOrderPosition;
        }

        BlockPos bestAnchor = plannedWorkTargets.get(0);
        double bestScore = Double.NEGATIVE_INFINITY;
        for (final BlockPos candidate : plannedWorkTargets)
        {
            final int covered = countCoveredTargets(candidate);
            final double score = covered * 100.0D
                                   - BlockPosUtil.getDistance2D(currentPosition, candidate) * 2.0D
                                   - BlockPosUtil.getDistance2D(workOrderPosition, candidate) * 0.25D
                                   + (candidate.equals(plannedRouteTarget) ? 1.0D : 0.0D);
            if (score > bestScore)
            {
                bestScore = score;
                bestAnchor = candidate;
            }
        }
        return bestAnchor;
    }

    private int countCoveredTargets(final BlockPos workPosition)
    {
        int covered = 0;
        for (final BlockPos target : plannedWorkTargets)
        {
            if (isWorkTargetReachableFrom(workPosition, target))
            {
                covered++;
            }
        }
        return covered;
    }

    private boolean isCurrentPlanTargetStillRelevant(final BuildingStructureHandler.Stage stage)
    {
        final BlockPos target = plannedWorkTargets.get(0);
        final boolean includeEntities = stage == BuildingStructureHandler.Stage.SPAWN || stage == BuildingStructureHandler.Stage.REMOVE;
        final boolean removing = stage == BuildingStructureHandler.Stage.REMOVE;
        final BlockPos localPos = structurePlacer.getB().getStructurePosFromWorld(target);
        final BlueprintPositionInfo info = structurePlacer.getB().getBluePrint().getBluePrintPositionInfo(localPos, includeEntities);
        return info != null
                 && info.getBlockInfo() != null
                 && !world.isOutsideBuildHeight(target)
                 && isUpcomingWorkOperation(stage, info, target, includeEntities, removing);
    }

    private boolean isWorkTargetReachableFrom(final BlockPos workPosition, final BlockPos target)
    {
        return BlockPosUtil.getDistance2D(workPosition, target) <= STANDARD_WORKING_RANGE
                 && Math.abs(workPosition.getY() - target.getY()) <= 3;
    }

    private boolean isWorkPositionSafe(final BlockPos workPosition)
    {
        if (workPosition == null || !world.getFluidState(workPosition).isEmpty())
        {
            return false;
        }

        final boolean alreadyStandingAtPosition = worker.blockPosition().equals(workPosition);
        final BlockPos support = workPosition.below();
        if (!alreadyStandingAtPosition
              && SurfaceType.getSurfaceType(world, world.getBlockState(support), support, new PathingOptions()) != SurfaceType.WALKABLE)
        {
            return false;
        }

        final double dx = workPosition.getX() + 0.5D - worker.getX();
        final double dy = workPosition.getY() - worker.getY();
        final double dz = workPosition.getZ() + 0.5D - worker.getZ();
        final AABB candidateBox = worker.getBoundingBox().move(dx, dy, dz);
        return world.noCollision(worker, candidateBox);
    }

    private void resetWorkPlanner(final String reason, final boolean clearWorkPosition)
    {
        final boolean hadPlan = workPlanValid || plannedWorkStage != null || plannedProgressCursor != null || !plannedWorkTargets.isEmpty();
        if (workPositionPath != null)
        {
            workPositionPath.cancel();
            workPositionPath = null;
        }
        if (clearWorkPosition)
        {
            updateWorkFrom(null);
        }
        plannedWorkTargets.clear();
        plannedWorkStage = null;
        plannedProgressCursor = null;
        plannedWorkOrderPosition = null;
        plannedRouteTarget = null;
        workPlanValid = false;
        workPlanStageExhausted = false;
        plannerInvalidationReason = reason;
        if (hadPlan)
        {
            MinecoloniesAIMetrics.recordBuilderPlannerInvalidation(reason);
        }
    }

    /**
     * Describes the current cached planner state for focused GameTest diagnostics.
     *
     * @return concise planner state.
     */
    protected final String getWorkPlannerDebugSummary()
    {
        final String pathState = workPositionPath == null
                                   ? "none"
                                   : "done=" + workPositionPath.isDone()
                                       + ",cancelled=" + workPositionPath.isCancelled()
                                       + ",path=" + workPositionPath.getPath();
        return "valid=" + workPlanValid
                 + ",stage=" + plannedWorkStage
                 + ",progress=" + plannedProgressCursor
                 + ",targets=" + plannedWorkTargets
                 + ",stageExhausted=" + workPlanStageExhausted
                 + ",route=" + plannedRouteTarget
                 + ",workFrom=" + workFrom
                 + ",invalidation=" + plannerInvalidationReason
                 + ",pathResult=" + pathState;
    }

    @Override
    public void resetCurrentStructure()
    {
        super.resetCurrentStructure();
        resetWorkPlanner("structure_reset", true);
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return false;
    }

    @Override
    public int getBlockMiningDelay(@NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        return (int) (super.getBlockMiningDelay(state, pos) * SPEED_BUFF_0);
    }

    /**
     * Calculates after how many actions the AI should dump its inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }

    @Override
    protected void sendCompletionMessage(final IWorkOrder wo)
    {
        super.sendCompletionMessage(wo);

        final BlockPos position = wo.getLocation();
        boolean showManualSuffix = false;
        if (building.getManualMode())
        {
            showManualSuffix = true;
            for (final IWorkOrder workorder : building.getColony().getWorkManager().getWorkOrders().values())
            {
                if (workorder.getID() != wo.getID() && workorder.isClaimedBy(worker.getCitizenData()))
                {
                    showManualSuffix = false;
                }
            }
        }

        MutableComponent message;
        switch (wo.getWorkOrderType())
        {
            case REPAIR:
                message = Component.translatable(
                  COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_REPAIRING_COMPLETE,
                  wo.getDisplayName(),
                  position.getX(),
                  position.getY(),
                  position.getZ());
                break;
            case REMOVE:
                message = Component.translatable(
                  COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_DECONSTRUCTION_COMPLETE,
                  wo.getDisplayName(),
                  position.getX(),
                  position.getY(),
                  position.getZ());
                break;
            default:
                message = Component.translatable(
                  COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILD_COMPLETE,
                  wo.getDisplayName(),
                  position.getX(),
                  position.getY(),
                  position.getZ());
                break;
        }

        if (showManualSuffix)
        {
            message.append(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_MANUAL_SUFFIX));
        }

        worker.getCitizenChatHandler().sendLocalizedChat(message);
    }
}
