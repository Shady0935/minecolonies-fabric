package com.minecolonies.fabric.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Runtime stand-in for Forge's per-entity persistent-data attachment. */
public final class EntityDataHooks
{
    private static final Map<Entity, CompoundTag> DATA = Collections.synchronizedMap(new WeakHashMap<>());

    private EntityDataHooks()
    {
    }

    public static CompoundTag getPersistentData(final Entity entity)
    {
        return DATA.computeIfAbsent(entity, ignored -> new CompoundTag());
    }
}
