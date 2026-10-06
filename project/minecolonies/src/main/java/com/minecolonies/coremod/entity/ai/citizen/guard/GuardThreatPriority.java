package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.world.entity.LivingEntity;

/**
 * Priorities shared by guards when they first discover a hostile.
 */
final class GuardThreatPriority
{
    private static final int ATTACKING_CITIZEN_PRIORITY = 70;
    private static final int ATTACKING_GUARD_PRIORITY   = 55;
    private static final int COLONY_RAIDER_PRIORITY     = 35;
    private static final int RECENT_ATTACK_TICKS        = 20 * 10;

    private GuardThreatPriority()
    {
    }

    /**
     * Get the one-time threat bonus for a newly discovered hostile.
     *
     * @param guard the guard doing the scan
     * @param threat the hostile candidate
     * @return the priority bonus
     */
    static int getInitialPriority(final EntityCitizen guard, final LivingEntity threat)
    {
        final IColony colony = guard.getCitizenColonyHandler().getColony();
        if (colony == null)
        {
            return 0;
        }

        final int lastHurtMobTimestamp = threat.getLastHurtMobTimestamp();
        if (lastHurtMobTimestamp > 0 && threat.tickCount - lastHurtMobTimestamp >= 0
              && threat.tickCount - lastHurtMobTimestamp <= RECENT_ATTACK_TICKS
              && threat.getLastHurtMob() instanceof EntityCitizen citizen
              && citizen.getCitizenData() != null
              && citizen.getCitizenColonyHandler().getColony() == colony)
        {
            return citizen.getCitizenData().getJob() instanceof AbstractJobGuard
                     ? ATTACKING_GUARD_PRIORITY
                     : ATTACKING_CITIZEN_PRIORITY;
        }

        if (threat instanceof AbstractEntityRaiderMob raider
              && raider.getColony() == colony
              && colony.isCoordInColony(guard.level(), threat.blockPosition()))
        {
            return COLONY_RAIDER_PRIORITY;
        }

        return 0;
    }
}
