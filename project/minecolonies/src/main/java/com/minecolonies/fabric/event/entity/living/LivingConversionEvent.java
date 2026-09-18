package com.minecolonies.fabric.event.entity.living;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class LivingConversionEvent extends Event
{
    private final LivingEntity entity;
    protected LivingConversionEvent(final LivingEntity entity) { this.entity = entity; }
    public LivingEntity getEntity() { return entity; }

    public static class Pre extends LivingConversionEvent
    {
        private final EntityType<?> outcome;
        public Pre(final LivingEntity entity, final EntityType<?> outcome) { super(entity); this.outcome = outcome; }
        public EntityType<?> getOutcome() { return outcome; }
    }
}
