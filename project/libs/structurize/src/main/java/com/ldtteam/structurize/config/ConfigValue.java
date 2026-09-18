package com.ldtteam.structurize.config;

/** Mutable value wrapper replacing ForgeConfigSpec values on Fabric. */
public final class ConfigValue<T>
{
    private T value;

    public ConfigValue(final T value)
    {
        this.value = value;
    }

    public T get()
    {
        return value;
    }

    public void set(final T value)
    {
        this.value = value;
    }
}
