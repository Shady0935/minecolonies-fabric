package com.ldtteam.structurize.config;

/** Mutable value wrapper replacing ForgeConfigSpec values on Fabric. */
public class ConfigValue<T> extends com.ldtteam.structurize.config.fabric.FabricConfigSpec.ConfigValue<T>
{
    public ConfigValue(final T value)
    {
        super(value);
    }
}
