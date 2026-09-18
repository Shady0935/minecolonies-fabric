package com.minecolonies.fabric.event.level;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.core.BlockPos;

import java.util.List;

public class ExplosionEvent extends Event
{
    private final Level level;
    private final Explosion explosion;

    protected ExplosionEvent(final Level level, final Explosion explosion)
    {
        this.level = level; this.explosion = explosion;
    }
    public Level getLevel() { return level; }
    public Explosion getExplosion() { return explosion; }

    public static class Start extends ExplosionEvent
    {
        public Start(final Level level, final Explosion explosion) { super(level, explosion); }
    }

    public static class Detonate extends ExplosionEvent
    {
        private final List<BlockPos> affectedBlocks;
        private final List<Entity> affectedEntities;
        public Detonate(final Level level, final Explosion explosion, final List<BlockPos> affectedBlocks, final List<Entity> affectedEntities)
        {
            super(level, explosion); this.affectedBlocks = affectedBlocks; this.affectedEntities = affectedEntities;
        }
        public List<BlockPos> getAffectedBlocks() { return affectedBlocks; }
        public List<Entity> getAffectedEntities() { return affectedEntities; }
    }
}
