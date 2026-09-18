package com.minecolonies.fabric.event.entity;

import com.minecolonies.fabric.event.Event;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityTravelToDimensionEvent extends Event
{
    private final Entity entity;
    private final ResourceKey<Level> dimension;
    public EntityTravelToDimensionEvent(final Entity entity, final ResourceKey<Level> dimension) { this.entity = entity; this.dimension = dimension; }
    public Entity getEntity() { return entity; }
    public ResourceKey<Level> getDimension() { return dimension; }
}
