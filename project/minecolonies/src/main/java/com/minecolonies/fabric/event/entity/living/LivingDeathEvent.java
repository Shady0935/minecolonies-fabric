package com.minecolonies.fabric.event.entity.living;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

public class LivingDeathEvent extends Event
{
    private final LivingEntity entity;
    public LivingDeathEvent(final LivingEntity entity) { this.entity = entity; }
    public LivingEntity getEntity() { return entity; }
    public DamageSource getSource()
    {
        final DamageSource source = entity.getLastDamageSource();
        return source == null ? entity.damageSources().generic() : source;
    }
}
