package com.minecolonies.api.entity.mobs.egyptians;

import net.minecraft.commands.CommandSource;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.nbt.CompoundTag;
import com.minecolonies.fabric.capability.ICapabilitySerializable;

public interface IEgyptianEntity extends Enemy, CommandSource, ICapabilitySerializable<CompoundTag>
{
}
