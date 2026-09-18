package com.minecolonies.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/** Compatibility event used by initializers while they are called explicitly by Fabric. */
public final class RegisterEvent
{
    private final ResourceKey<? extends Registry<?>> registryKey;
    private final FabricRegistry<?> fabricRegistry;

    public RegisterEvent(final ResourceKey<? extends Registry<?>> registryKey, final FabricRegistry<?> fabricRegistry)
    {
        this.registryKey = registryKey;
        this.fabricRegistry = fabricRegistry;
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey()
    {
        return registryKey;
    }

    @SuppressWarnings("unchecked")
    public <T> FabricRegistry<T> getFabricRegistry()
    {
        return (FabricRegistry<T>) fabricRegistry;
    }
}
