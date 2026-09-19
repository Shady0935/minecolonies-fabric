package com.minecolonies.fabric.event.entity;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;

/** Fabric-side equivalent of Forge's cancellable projectile-impact event. */
public class ProjectileImpactEvent extends Event
{
    private final Entity projectile;
    private final HitResult hitResult;

    public ProjectileImpactEvent(final Entity projectile, final HitResult hitResult)
    {
        this.projectile = projectile;
        this.hitResult = hitResult;
    }

    public Entity getProjectile()
    {
        return projectile;
    }

    public HitResult getHitResult()
    {
        return hitResult;
    }
}
