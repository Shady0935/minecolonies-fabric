package com.minecolonies.fabric.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Supplier;

/** Eager Fabric equivalent of a Forge FabricRegistryObject. */
public final class FabricRegistryObject<T> implements Supplier<T>
{
    private final ResourceLocation id;
    private final T value;

    FabricRegistryObject(final ResourceLocation id, final T value)
    {
        this.id = id;
        this.value = value;
    }

    @Override
    public T get()
    {
        return value;
    }

    public ResourceLocation getId()
    {
        return id;
    }

    public Optional<T> getOptional()
    {
        return Optional.ofNullable(value);
    }

    public boolean isPresent()
    {
        return value != null;
    }
}
