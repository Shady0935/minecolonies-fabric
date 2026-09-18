package com.minecolonies.fabric.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Lightweight typed configuration tree for the 1.20.1 Fabric port.
 * Values are deliberately live objects with the same get/set surface the
 * gameplay code used on Forge; persistence is handled by the Fabric entrypoint
 * when the runtime config integration is added.
 */
public final class FabricConfigSpec
{
    public static class ConfigValue<T>
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

    public static final class BooleanValue extends ConfigValue<Boolean>
    {
        public BooleanValue(final boolean value) { super(value); }
    }

    public static final class IntValue extends ConfigValue<Integer>
    {
        private final int min;
        private final int max;

        public IntValue(final int value, final int min, final int max)
        {
            super(value);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(final Integer value)
        {
            super.set(Math.max(min, Math.min(max, value)));
        }
    }

    public static final class LongValue extends ConfigValue<Long>
    {
        private final long min;
        private final long max;

        public LongValue(final long value, final long min, final long max)
        {
            super(value);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(final Long value)
        {
            super.set(Math.max(min, Math.min(max, value)));
        }
    }

    public static final class DoubleValue extends ConfigValue<Double>
    {
        private final double min;
        private final double max;

        public DoubleValue(final double value, final double min, final double max)
        {
            super(value);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(final Double value)
        {
            super.set(Math.max(min, Math.min(max, value)));
        }
    }

    public static final class EnumValue<V extends Enum<V>> extends ConfigValue<V>
    {
        public EnumValue(final V value) { super(value); }
    }

    public static final class Builder
    {
        private final ArrayDeque<String> categories = new ArrayDeque<>();

        public Builder comment(final String ignored)
        {
            return this;
        }

        public Builder translation(final String ignored)
        {
            return this;
        }

        public Builder push(final String category)
        {
            categories.push(category);
            return this;
        }

        public Builder pop()
        {
            if (!categories.isEmpty()) categories.pop();
            return this;
        }

        public BooleanValue define(final String key, final boolean value)
        {
            return new BooleanValue(value);
        }

        public IntValue define(final String key, final int value)
        {
            return new IntValue(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public LongValue define(final String key, final long value)
        {
            return new LongValue(value, Long.MIN_VALUE, Long.MAX_VALUE);
        }

        public DoubleValue define(final String key, final double value)
        {
            return new DoubleValue(value, -Double.MAX_VALUE, Double.MAX_VALUE);
        }

        public IntValue defineInRange(final String key, final int value, final int min, final int max)
        {
            return new IntValue(value, min, max);
        }

        public LongValue defineInRange(final String key, final long value, final long min, final long max)
        {
            return new LongValue(value, min, max);
        }

        public DoubleValue defineInRange(final String key, final double value, final double min, final double max)
        {
            return new DoubleValue(value, min, max);
        }

        public <T> ConfigValue<List<? extends T>> defineList(final String key, final List<? extends T> values, final Predicate<Object> validator)
        {
            final ArrayList<T> copy = new ArrayList<>();
            for (final T value : values)
            {
                if (validator == null || validator.test(value)) copy.add(value);
            }
            return new ConfigValue<>(copy);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(final String key, final V value)
        {
            return new EnumValue<>(Objects.requireNonNull(value));
        }
    }
}
