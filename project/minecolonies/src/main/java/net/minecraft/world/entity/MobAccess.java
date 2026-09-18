package net.minecraft.world.entity;

import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;

/**
 * Narrow access bridge for the few former Forge access-transformer call sites.
 * Keeping it in the vanilla package avoids widening fields globally.
 */
public final class MobAccess
{
    private MobAccess()
    {
    }

    public static GoalSelector goalSelector(final Mob mob)
    {
        return mob.goalSelector;
    }

    public static GoalSelector targetSelector(final Mob mob)
    {
        return mob.targetSelector;
    }

    public static void setMoveControl(final Mob mob, final MoveControl control)
    {
        mob.moveControl = control;
    }
}
