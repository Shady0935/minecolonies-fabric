package com.minecolonies.fabric.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntityAttributeCreationEvent extends Event
{
    private final Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes = new LinkedHashMap<>();

    public void put(final EntityType<? extends LivingEntity> type, final AttributeSupplier supplier)
    {
        attributes.put(type, supplier);
    }

    public Map<EntityType<? extends LivingEntity>, AttributeSupplier> getAttributes()
    {
        return Map.copyOf(attributes);
    }
}
