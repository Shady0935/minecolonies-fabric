package com.minecolonies.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Eager registration helper.  Fabric has no mod-event registration phase, so
 * entries are created and registered at the call site while onInitialize is
 * running.  The event-bus overload is retained only as a source-compatibility
 * bridge for files not yet converted to explicit init methods.
 */
public final class FabricDeferredRegister<T>
{
    private final String modId;
    private final ResourceLocation registryId;
    private FabricRegistry<T> registry;
    private final List<FabricRegistryObject<T>> values = new ArrayList<>();

    private FabricDeferredRegister(final ResourceLocation registryId, final String modId)
    {
        this.registryId = registryId;
        this.modId = modId;
    }

    private FabricDeferredRegister(final FabricRegistry<T> registry, final String modId)
    {
        this.registry = registry;
        this.registryId = registry.getRegistryName();
        this.modId = modId;
    }

    public static <T> FabricDeferredRegister<T> create(final ResourceLocation registryId, final String modId)
    {
        return new FabricDeferredRegister<>(registryId, modId);
    }

    public static <T> FabricDeferredRegister<T> create(final FabricRegistry<T> registry, final String modId)
    {
        return new FabricDeferredRegister<>(registry, modId);
    }

    public static <T> FabricDeferredRegister<T> create(final ResourceKey<? extends Registry<T>> key, final String modId)
    {
        return new FabricDeferredRegister<>(FabricRegistries.byVanillaKey(key), modId);
    }

    @SuppressWarnings("unchecked")
    public <U extends T> FabricRegistryObject<U> register(final String path, final Supplier<? extends U> supplier)
    {
        final U value = Objects.requireNonNull(supplier.get(), "Registry value for " + path + " was null");
        if (registry == null)
        {
            registry = FabricRegistries.custom(registryId, value.getClass());
        }
        final ResourceLocation id = new ResourceLocation(modId, path);
        registry.register(id, value);
        final FabricRegistryObject<U> object = new FabricRegistryObject<>(id, value);
        values.add((FabricRegistryObject<T>) (FabricRegistryObject<?>) object);
        return object;
    }

    public void register(final Object ignoredEventBus)
    {
        // Values are registered eagerly by register().
    }

    public List<FabricRegistryObject<T>> values()
    {
        return List.copyOf(values);
    }
}
