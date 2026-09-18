package com.minecolonies.fabric.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * Small Fabric-side registry facade used while moving the 1.20.1 code away from
 * Forge's registry interfaces.  It deliberately delegates to Minecraft's
 * Registry implementation, so values remain visible to vanilla/Fabric lookup,
 * tags and network id machinery.
 */
public final class FabricRegistry<T> implements Iterable<T>
{
    private final Registry<T> delegate;
    private final ResourceLocation registryId;
    private final ResourceLocation defaultKey;

    public FabricRegistry(final Registry<T> delegate)
    {
        this(delegate, null, null);
    }

    public FabricRegistry(final Registry<T> delegate, final ResourceLocation registryId, final ResourceLocation defaultKey)
    {
        this.delegate = delegate;
        this.registryId = registryId;
        this.defaultKey = defaultKey;
    }

    public Registry<T> raw()
    {
        return delegate;
    }

    public ResourceLocation getRegistryName()
    {
        return registryId;
    }

    public ResourceLocation getDefaultKey()
    {
        return defaultKey;
    }

    public ResourceKey<Registry<T>> key()
    {
        return (ResourceKey<Registry<T>>) (ResourceKey<?>) delegate.key();
    }

    public T getValue(final ResourceLocation id)
    {
        return delegate.get(id);
    }

    public T getValue(final ResourceKey<T> key)
    {
        return delegate.get(key);
    }

    public ResourceLocation getKey(final T value)
    {
        return delegate.getKey(value);
    }

    public int getID(final T value)
    {
        return delegate.getId(value);
    }

    public boolean containsKey(final ResourceLocation id)
    {
        return delegate.containsKey(id);
    }

    public Collection<T> getValues()
    {
        return StreamSupport.stream(delegate.spliterator(), false).toList();
    }

    public Set<Map.Entry<ResourceKey<T>, T>> getEntries()
    {
        final Set<Map.Entry<ResourceKey<T>, T>> result = new LinkedHashSet<>();
        for (final ResourceLocation id : delegate.keySet())
        {
            final T value = delegate.get(id);
            if (value != null)
            {
                final ResourceKey<T> key = ResourceKey.create((ResourceKey<? extends Registry<T>>) (ResourceKey<?>) delegate.key(), id);
                result.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
            }
        }
        return result;
    }

    public Optional<Holder.Reference<T>> getHolder(final ResourceLocation id)
    {
        return delegate.getHolder(ResourceKey.create((ResourceKey<? extends Registry<T>>) (ResourceKey<?>) delegate.key(), id));
    }

    public Optional<Holder.Reference<T>> getHolder(final ResourceKey<T> key)
    {
        return delegate.getHolder(key);
    }

    public Holder.Reference<T> getHolderOrThrow(final ResourceKey<T> key)
    {
        return delegate.getHolderOrThrow(key);
    }

    public Optional<ResourceKey<T>> getResourceKey(final T value)
    {
        return delegate.getResourceKey(value);
    }

    public FabricRegistryTags<T> tags()
    {
        return new FabricRegistryTags<>(delegate);
    }

    public T register(final ResourceLocation id, final T value)
    {
        return Registry.register(delegate, id, value);
    }

    public T register(final ResourceKey<T> key, final T value)
    {
        return Registry.register(delegate, key.location(), value);
    }

    public void registerAll(final Map<ResourceLocation, Supplier<? extends T>> values)
    {
        values.forEach((id, supplier) -> register(id, supplier.get()));
    }

    @Override
    public java.util.Iterator<T> iterator()
    {
        return getValues().iterator();
    }
}
