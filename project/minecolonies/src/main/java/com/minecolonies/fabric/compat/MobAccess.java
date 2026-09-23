package com.minecolonies.fabric.compat;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;

/** Narrow access bridge for the former Forge access-transformer call sites. */
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
