package com.minecolonies.fabric.event.entity;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityJoinLevelEvent extends Event
{
    private final Entity entity;
    private final Level level;
    public EntityJoinLevelEvent(final Entity entity, final Level level) { this.entity = entity; this.level = level; }
    public Entity getEntity() { return entity; }
    public Level getLevel() { return level; }
}
