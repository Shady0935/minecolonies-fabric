package com.minecolonies.fabric.event.entity.living;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingHurtEvent extends Event
{
    private final LivingEntity entity;
    private final DamageSource source;
    public LivingHurtEvent(final LivingEntity entity, final DamageSource source) { this.entity = entity; this.source = source; }
    public LivingEntity getEntity() { return entity; }
    public DamageSource getSource() { return source; }
}
