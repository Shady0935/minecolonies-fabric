package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.combat.threat.ThreatTableEntry;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.PathingOptions;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobDruid;
import com.minecolonies.coremod.entity.DruidPotionEntity;
import com.minecolonies.coremod.entity.ai.combat.AttackMoveAI;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.AbstractPathJob;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobCanSee;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobMoveAwayFromLocation;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobMoveToLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiPredicate;

import static com.minecolonies.api.research.util.ResearchConstants.DRUID_USE_POTIONS;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.StatisticsConstants.MOBS_KILLED;
import static com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIFight.SPEED_LEVEL_BONUS;

/**
 * Druid combat AI
 */
public class DruidCombatAI extends AttackMoveAI<EntityCitizen>
{
    /**
     * Short reservations keep nearby Druids from throwing support potions at
     * the same ally while the first potion is still in flight. Keys are entity
     * ids rather than entity references, and the Level keys are weak.
     */
    private static final Map<Level, Map<Integer, SupportReservation>> SUPPORT_TARGET_RESERVATIONS = new WeakHashMap<>();

    /**
     * How long a support target stays reserved while its potion travels.
     */
    private static final long SUPPORT_RESERVATION_TICKS = 60;

    /**
     * List of potential positive effects.
     */
    private static final ImmutableList<MobEffect> SUPPORT_EFFECTS = ImmutableList.of(MobEffects.DAMAGE_BOOST, MobEffects.SATURATION, MobEffects.HEAL, MobEffects.DAMAGE_RESISTANCE);

    /**
     * List of potential positive effects.
     */
    private static final ImmutableList<MobEffect> ADVERSE_EFFECTS = ImmutableList.of(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS);

    /**
     * The xp per thrown potion
     */
    private static final double PER_POTION_XP = 0.05D;

    /**
     * The value of the speed which the guard will move.
     */
    private static final double COMBAT_SPEED = 1.0;

    /**
     * Potion velocity.
     */
    public static final float POTION_VELOCITY = 0.5f;

    /**
     * Flee chance
     */
    private static final int FLEE_CHANCE = 3;

    /**
     * The parent combat AI.
     */
    private final AbstractEntityAIGuard<JobDruid, AbstractBuildingGuards> parentAI;

    /**
     * The combat pathing options.
     */
    private final PathingOptions    combatPathingOptions;

    /**
     * If the last attack was an instant effect potion.
     */
    private boolean instantEffect;

    public DruidCombatAI(
      final EntityCitizen owner,
      final ITickRateStateMachine stateMachine,
      final AbstractEntityAIGuard parentAI)
    {
        super(owner, stateMachine);

        this.parentAI = parentAI;
        combatPathingOptions = new PathingOptions();
        combatPathingOptions.setEnterDoors(true);
        combatPathingOptions.setCanOpenDoors(true);
        combatPathingOptions.setCanSwim(true);
        combatPathingOptions.withOnPathCost(0.8);
        combatPathingOptions.withJumpCost(0.01);
        combatPathingOptions.withDropCost(1.5);
    }

    @Override
    protected void doAttack(final LivingEntity target)
    {
        if (user.distanceToSqr(target) < RANGED_FLEE_SQDIST)
        {
            if (user.getRandom().nextInt(FLEE_CHANCE) == 0 &&
                  !((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getTask().equals(GuardTaskSetting.GUARD))
            {
                user.getNavigation().moveAwayFromLivingEntity(target, getAttackDistance() / 2.0, getCombatMovementSpeed());
            }
        }
        else
        {
            user.getNavigation().stop();
        }

        user.swing(InteractionHand.MAIN_HAND);

        final int level = user.getCitizenData().getCitizenSkillHandler().getLevel(ModGuardTypes.druid.get().getSecondarySkill());
        final int time = user.getCitizenData().getCitizenSkillHandler().getLevel(ModGuardTypes.druid.get().getPrimarySkill()) * 20;

        final float inaccuracy = 99f / level;
        final MobEffect effect;
        final ItemStack stack = new ItemStack(Items.SPLASH_POTION);
        boolean gotMaterial = false;
        BiPredicate<LivingEntity, MobEffect> predicate;
        if (user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(DRUID_USE_POTIONS) > 0
              && InventoryUtils.hasItemInItemHandler(user.getInventoryCitizen(), item -> item.getItem() == ModItems.magicpotion))
        {
            gotMaterial = true;
        }
        final boolean hostileTarget = AbstractEntityAIGuard.isAttackableTarget(user, target);
        if (hostileTarget)
        {
            effect = ADVERSE_EFFECTS.get(user.getRandom().nextInt(gotMaterial ? 2 : 1));
            predicate = (entity, eff) -> AbstractEntityAIGuard.isAttackableTarget(user, entity);
        }
        else
        {
            if (gotMaterial && isSupportAlly(target) && target.getHealth() <= target.getMaxHealth() * 0.5f)
            {
                effect = MobEffects.HEAL;
            }
            else
            {
                effect = SUPPORT_EFFECTS.get(user.getRandom().nextInt(gotMaterial ? 4 : 1));
            }
            predicate = (entity, eff) -> !AbstractEntityAIGuard.isAttackableTarget(user, entity);
        }

        PotionUtils.setCustomEffects(stack, Collections.singleton(new MobEffectInstance(effect, time, gotMaterial ? 2 : 0)));
        if (!hostileTarget && isSupportAlly(target))
        {
            reserveSupportTarget(user.level(), target, user);
        }
        DruidPotionEntity.throwPotionAt(stack, target, user, user.getCommandSenderWorld(), POTION_VELOCITY, inaccuracy, predicate);

        if (gotMaterial)
        {
            InventoryUtils.removeStackFromItemHandler(user.getCitizenData().getInventory(), new ItemStack(ModItems.magicpotion, 1), 1);
        }

        this.instantEffect = effect.isInstantenous();

        user.setItemInHand(InteractionHand.MAIN_HAND, stack);

        user.getThreatTable().removeCurrentTarget();

        user.decreaseSaturationForContinuousAction();
        user.getCitizenExperienceHandler().addExperience(PER_POTION_XP);
    }

    @Override
    protected int getAttackDelay()
    {
        return this.instantEffect ? super.getAttackDelay() * 2 : super.getAttackDelay();
    }

    @Override
    protected double getAttackDistance()
    {
        int attackDist = BASE_DISTANCE_FOR_POTION_ATTACK;
        // + 1 Blockrange per building level for a total of +5 from building level
        if (user.getCitizenData().getWorkBuilding() != null)
        {
            attackDist += user.getCitizenData().getWorkBuilding().getBuildingLevel();
        }

        if (target != null)
        {
            attackDist += user.getY() - target.getY();
        }

        return attackDist;
    }

    @Override
    protected PathResult moveInAttackPosition(final LivingEntity target)
    {
        if (BlockPosUtil.getDistanceSquared(target.blockPosition(), user.blockPosition()) <= 4.0)
        {
            final PathJobMoveAwayFromLocation job = new PathJobMoveAwayFromLocation(user.level(),
              AbstractPathJob.prepareStart(target),
              target.blockPosition(),
              12,
              (int) user.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
              user);
            final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed(), true);
            job.setPathingOptions(combatPathingOptions);
            return pathResult;
        }
        else if (BlockPosUtil.getDistance2D(target.blockPosition(), user.blockPosition()) >= 20)
        {
            final PathJobMoveToLocation job = new PathJobMoveToLocation(user.level(), AbstractPathJob.prepareStart(user), target.blockPosition(), 200, user);
            final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed(), true);
            job.setPathingOptions(combatPathingOptions);
            return pathResult;
        }
        final PathJobCanSee job = new PathJobCanSee(user, target, user.level(), ((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getGuardPos(), 40);
        final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed(), true);
        job.setPathingOptions(combatPathingOptions);
        return pathResult;
    }

    /**
     * Get combat speed
     *
     * @return movent speed
     */
    protected double getCombatMovementSpeed()
    {
        double levelAdjustment = user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Mana) * SPEED_LEVEL_BONUS;
        levelAdjustment += (user.getCitizenData().getWorkBuilding().getBuildingLevel() - 1) * SPEED_LEVEL_BONUS;

        levelAdjustment = Math.min(levelAdjustment, 0.3);
        return COMBAT_SPEED + levelAdjustment;
    }

    @Override
    protected boolean isAttackableTarget(final LivingEntity entity)
    {
        return (AbstractEntityAIGuard.isAttackableTarget(user, entity)
                  || (entity instanceof IThreatTableEntity && ((IThreatTableEntity) entity).getThreatTable().getTarget() != null )
                  || (entity instanceof Player && entity.getLastHurtByMobTimestamp() != 0 && entity.tickCount > entity.getLastHurtByMobTimestamp() && entity.tickCount - entity.getLastHurtByMobTimestamp() < 20 * 30)
                  || isSupportAlly(entity))
                 && !wasAffectedByDruid(entity);
    }

    @Override
    protected int getAdditionalThreat(final LivingEntity entity)
    {
        final int hostilePriority = GuardThreatPriority.getInitialPriority(user, entity);
        if (hostilePriority > 0)
        {
            return hostilePriority;
        }

        if (!isSupportAlly(entity))
        {
            return 0;
        }

        final EntityCitizen ally = (EntityCitizen) entity;
        final float healthRatio = ally.getHealth() / ally.getMaxHealth();
        if (healthRatio <= 0.3f)
        {
            return 120;
        }
        if (isGuardInCombat(ally))
        {
            return 95;
        }

        return 65 + Math.round((1.0f - healthRatio) * 20);
    }

    @Override
    protected boolean checkForTarget()
    {
        final ThreatTableEntry current = user.getThreatTable().getTarget();
        if (current != null && isSupportAlly(current.getEntity())
              && isSupportTargetReservedByOther(user.level(), current.getEntity(), user))
        {
            if (target == null)
            {
                user.getThreatTable().removeCurrentTarget();
            }
            else
            {
                resetTarget();
            }
            return false;
        }

        return super.checkForTarget();
    }

    @Override
    protected boolean searchNearbyTarget()
    {
        if (checkForTarget())
        {
            return true;
        }

        final List<LivingEntity> entities = user.level().getEntitiesOfClass(LivingEntity.class, getSearchArea());

        if (entities.isEmpty())
        {
            return false;
        }

        int targetsUnderEffect = 0;
        boolean foundTarget = false;
        final Set<Integer> targetsHandledByOtherDruids = new HashSet<>();
        for (final LivingEntity entity : entities)
        {
            if (entity instanceof EntityCitizen otherDruid && otherDruid != user
                  && otherDruid.getCitizenData() != null && otherDruid.getCitizenData().getJob() instanceof JobDruid)
            {
                final ThreatTableEntry otherTarget = otherDruid.getThreatTable().getTarget();
                if (otherTarget != null && otherTarget.getEntity().isAlive())
                {
                    targetsHandledByOtherDruids.add(otherTarget.getEntity().getId());
                }
            }
        }

        for (final LivingEntity entity : entities)
        {
            if (!entity.isAlive())
            {
                continue;
            }

            if (skipSearch(entity))
            {
                return false;
            }

            if (isSupportAlly(entity) && (targetsHandledByOtherDruids.contains(entity.getId())
                  || isSupportTargetReservedByOther(user.level(), entity, user)))
            {
                continue;
            }

            if (isEntityValidTarget(entity))
            {
                if (user.hasLineOfSight(entity))
                {
                    addTargetThreat(entity);
                    foundTarget = true;
                }
            }
            else if (wasAffectedByDruid(entity))
            {
                targetsUnderEffect++;
            }
        }

        return foundTarget && targetsUnderEffect <= parentAI.building.getBuildingLevel() * 2;
    }

    /**
     * Whether an entity is a colony ally that can benefit from a support potion.
     */
    private boolean isSupportAlly(final LivingEntity entity)
    {
        if (!(entity instanceof EntityCitizen ally) || ally == user || ally.getCitizenData() == null)
        {
            return false;
        }

        final var colony = user.getCitizenColonyHandler().getColony();
        if (colony == null || ally.getCitizenColonyHandler().getColony() != colony)
        {
            return false;
        }

        return ally.getHealth() < ally.getMaxHealth() || isGuardInCombat(ally);
    }

    /**
     * Whether a colony guard has an active combat target.
     */
    private boolean isGuardInCombat(final EntityCitizen citizen)
    {
        return citizen.getCitizenData() != null && citizen.getCitizenData().getJob() instanceof AbstractJobGuard
                 && citizen.getThreatTable().getTargetMob() != null;
    }

    private static boolean isSupportTargetReservedByOther(final Level level, final LivingEntity target, final EntityCitizen druid)
    {
        synchronized (SUPPORT_TARGET_RESERVATIONS)
        {
            final Map<Integer, SupportReservation> reservations = SUPPORT_TARGET_RESERVATIONS.get(level);
            if (reservations == null)
            {
                return false;
            }

            final long gameTime = level.getGameTime();
            reservations.entrySet().removeIf(entry -> entry.getValue().expiresAt <= gameTime);
            final SupportReservation reservation = reservations.get(target.getId());
            return reservation != null && reservation.ownerEntityId != druid.getId();
        }
    }

    private static void reserveSupportTarget(final Level level, final LivingEntity target, final EntityCitizen druid)
    {
        synchronized (SUPPORT_TARGET_RESERVATIONS)
        {
            SUPPORT_TARGET_RESERVATIONS.computeIfAbsent(level, ignored -> new HashMap<>())
              .put(target.getId(), new SupportReservation(druid.getId(), level.getGameTime() + SUPPORT_RESERVATION_TICKS));
        }
    }

    private static final class SupportReservation
    {
        private final int ownerEntityId;
        private final long expiresAt;

        private SupportReservation(final int ownerEntityId, final long expiresAt)
        {
            this.ownerEntityId = ownerEntityId;
            this.expiresAt = expiresAt;
        }
    }

    /**
     * Check if an entity has one of the potion effects the druid hands out.
     * @param entity the entity to check for.
     * @return true if so.
     */
    private boolean wasAffectedByDruid(final LivingEntity entity)
    {
        return entity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || entity.hasEffect(MobEffects.SATURATION) || entity.hasEffect(MobEffects.DAMAGE_BOOST) || entity.hasEffect(MobEffects.WEAKNESS) || entity.hasEffect(MobEffects.DAMAGE_RESISTANCE) || entity.hasEffect(MobEffects.HEAL);
    }

    @Override
    protected boolean isWithinPersecutionDistance(final LivingEntity target)
    {
        return parentAI.isWithinPersecutionDistance(target.blockPosition(), getAttackDistance());
    }

    @Override
    protected boolean skipSearch(final LivingEntity entity)
    {
        // Found a sleeping guard nearby
        if (entity instanceof EntityCitizen && user.getRandom().nextInt(10) < 1)
        {
            final EntityCitizen citizen = (EntityCitizen) entity;
            if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && ((AbstractJobGuard<?>) citizen.getCitizenJobHandler().getColonyJob()).isAsleep()
                  && user.getSensing().hasLineOfSight(citizen))
            {
                parentAI.setWakeCitizen(citizen);
                return true;
            }
        }

        return false;
    }

    @Override
    protected int getYSearchRange()
    {
        if (((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getTask().equals(GuardTaskSetting.GUARD))
        {
            return Y_VISION + 25;
        }

        return Y_VISION;
    }

    @Override
    protected void onTargetDied(final LivingEntity entity)
    {
        parentAI.incrementActionsDoneAndDecSaturation();
        user.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
        user.getCitizenColonyHandler().getColony().getStatisticsManager().increment(MOBS_KILLED);
    }
}
