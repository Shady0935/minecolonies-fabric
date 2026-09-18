package com.minecolonies.fabric.util;

import java.util.Objects;
import java.util.function.Supplier;

/** Thread-safe lazy value used by the port in place of Forge's Lazy. */
public final class Lazy<T> implements Supplier<T>
{
    private final Supplier<? extends T> factory;
    private volatile T value;

    private Lazy(final Supplier<? extends T> factory)
    {
        this.factory = Objects.requireNonNull(factory);
    }

    public static <T> Lazy<T> of(final Supplier<? extends T> factory)
    {
        return new Lazy<>(factory);
    }

    @Override
    public T get()
    {
        T current = value;
        if (current == null)
        {
            synchronized (this)
            {
                current = value;
                if (current == null)
                {
                    value = current = Objects.requireNonNull(factory.get());
                }
            }
        }
        return current;
    }
}
