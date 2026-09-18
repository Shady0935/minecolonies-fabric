package com.minecolonies.fabric.capability;

import com.minecolonies.fabric.util.INBTSerializable;
import net.minecraft.nbt.Tag;

public interface ICapabilitySerializable<T extends Tag> extends ICapabilityProvider, INBTSerializable<T>
{
}
