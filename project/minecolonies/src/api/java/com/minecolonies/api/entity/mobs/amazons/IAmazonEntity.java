package com.minecolonies.api.entity.mobs.amazons;

import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.nbt.CompoundTag;
import com.minecolonies.fabric.capability.ICapabilitySerializable;

/**
 * A tagging interface for Amazon Entities.
 */
public interface IAmazonEntity extends Enemy, ICapabilitySerializable<CompoundTag>
{

}
