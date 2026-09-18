package com.minecolonies.fabric.event.entity.living;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;

public class MobSpawnEvent extends Event
{
    public static class PositionCheck extends MobSpawnEvent
    {
        private final Mob entity;
        private final LevelAccessor level;
        private final double x, y, z;
        private final MobSpawnType spawnType;
        public PositionCheck(final Mob entity, final LevelAccessor level, final double x, final double y, final double z, final MobSpawnType spawnType)
        {
            this.entity = entity; this.level = level; this.x = x; this.y = y; this.z = z; this.spawnType = spawnType;
        }
        public Mob getEntity() { return entity; }
        public LevelAccessor getLevel() { return level; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public MobSpawnType getSpawnType() { return spawnType; }
    }
}
