package com.ldtteam.structurize.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Small Fabric-side registry holder.  It deliberately keeps the upstream
 * {@code get()} call shape while the value is registered eagerly through the
 * vanilla registries.
 */
public final class RegistryEntry<T> implements Supplier<T>
{
    private final T value;

    public RegistryEntry(final T value)
    {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public T get()
    {
        return value;
    }
}
