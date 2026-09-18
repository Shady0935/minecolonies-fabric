package net.minecraftforge.registries;

import com.minecolonies.fabric.registry.FabricRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/** Source compatibility wrapper for the Fabric registry registration event. */
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
