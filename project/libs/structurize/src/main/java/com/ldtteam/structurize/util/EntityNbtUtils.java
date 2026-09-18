package com.ldtteam.structurize.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/**
 * Small bridge for the entity NBT API. Forge exposes serializeNBT/
 * deserializeNBT while vanilla 1.20.1 uses saveWithoutId/load.
 */
public final class EntityNbtUtils
{
    private EntityNbtUtils()
    {
    }

    public static CompoundTag save(final Entity entity)
    {
        final CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        final var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id != null)
        {
            tag.putString("id", id.toString());
        }
        return tag;
    }

    public static void load(final Entity entity, final CompoundTag tag)
    {
        entity.load(tag);
    }
}
