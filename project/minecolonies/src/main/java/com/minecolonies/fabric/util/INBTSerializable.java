package com.minecolonies.fabric.util;

public interface INBTSerializable<T>
{
    T serializeNBT();

    void deserializeNBT(T nbt);
}
