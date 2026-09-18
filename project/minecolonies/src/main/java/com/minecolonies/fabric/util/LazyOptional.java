package com.minecolonies.fabric.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Small Optional-like capability handle with Forge-compatible call shapes. */
public final class LazyOptional<T>
{
    private final Supplier<? extends T> factory;
    private volatile boolean resolved;
    private volatile boolean invalid;
    private volatile T value;

    private LazyOptional(final Supplier<? extends T> factory)
    {
        this.factory = Objects.requireNonNull(factory);
    }

    public static <T> LazyOptional<T> of(final Supplier<? extends T> factory)
    {
        return new LazyOptional<>(factory);
    }

    public static <T> LazyOptional<T> empty()
    {
        final LazyOptional<T> empty = new LazyOptional<>(() -> null);
        empty.resolved = true;
        return empty;
    }

    private T value()
    {
        if (!resolved)
        {
            synchronized (this)
            {
                if (!resolved)
                {
                    value = factory.get();
                    resolved = true;
                }
            }
        }
        return value;
    }

    public boolean isPresent()
    {
        return !invalid && value() != null;
    }

    public T orElse(final T fallback)
    {
        return isPresent() ? value : fallback;
    }

    public T orElseGet(final Supplier<? extends T> fallback)
    {
        return isPresent() ? value : fallback == null ? null : fallback.get();
    }

    /** Resolves this handle to the JDK Optional shape used by newer callers. */
    public Optional<T> resolve()
    {
        return isPresent() ? Optional.of(value) : Optional.empty();
    }

    public <U> Optional<U> map(final Function<? super T, ? extends U> mapper)
    {
        return resolve().map(mapper);
    }

    public T orElseThrow()
    {
        if (!isPresent()) throw new NoSuchElementException("Empty LazyOptional");
        return value;
    }

    public void ifPresent(final Consumer<? super T> consumer)
    {
        if (isPresent()) consumer.accept(value);
    }

    public <U> LazyOptional<U> cast()
    {
        return LazyOptional.of(() -> (U) value());
    }

    public <U> LazyOptional<U> lazyMap(final Function<? super T, ? extends U> mapper)
    {
        return LazyOptional.of(() -> isPresent() ? mapper.apply(value) : null);
    }

    public void invalidate()
    {
        invalid = true;
        value = null;
    }
}
