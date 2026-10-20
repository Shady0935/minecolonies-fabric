package com.ldtteam.structurize.config.fabric;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.LinkedHashMap;
import java.math.BigDecimal;
import java.util.function.Predicate;

/**
 * Lightweight typed configuration tree for the 1.20.1 Fabric port.
 * Values are deliberately live objects with the same get/set surface the
 * gameplay code used on Forge. The complete tree is persisted by FabricConfigFile
 * and validated before applying file values or server snapshots.
 */
public final class FabricConfigSpec
{
    public record Option(List<String> path, ConfigValue<?> value, Object defaultValue,
                         String comment, Number min, Number max, Predicate<Object> validator)
    {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Object normalize(final Object raw)
        {
            if (raw == null || !value.accepts(raw)) { throw new IllegalArgumentException("Missing or invalid option"); }
            if (defaultValue instanceof Boolean && raw instanceof Boolean) { return raw; }
            if (defaultValue instanceof String && raw instanceof String) { return raw; }
            if (defaultValue instanceof Number && raw instanceof Number number)
            {
                try
                {
                    final BigDecimal decimal = new BigDecimal(number.toString());
                    if (decimal.compareTo(new BigDecimal(min.toString())) >= 0 && decimal.compareTo(new BigDecimal(max.toString())) <= 0)
                    {
                        if (defaultValue instanceof Integer) { return decimal.intValueExact(); }
                        if (defaultValue instanceof Long) { return decimal.longValueExact(); }
                        if (Double.isFinite(decimal.doubleValue())) { return decimal.doubleValue(); }
                    }
                }
                catch (final ArithmeticException | NumberFormatException ignored) { }
            }
            if (defaultValue instanceof Enum<?> enumeration && raw instanceof String text)
            { return Enum.valueOf(enumeration.getDeclaringClass(), text); }
            if (defaultValue instanceof Enum<?> enumeration && enumeration.getDeclaringClass().isInstance(raw)) { return raw; }
            if (defaultValue instanceof List<?> defaults && raw instanceof List<?> list)
            {
                final List<Object> normalized = new ArrayList<>();
                for (final Object element : list)
                {
                    Object item = element;
                    if (!defaults.isEmpty() && defaults.get(0) instanceof Integer && element instanceof Number number)
                    {
                        try { item = new BigDecimal(number.toString()).intValueExact(); }
                        catch (final ArithmeticException exception) { throw new IllegalArgumentException("Invalid integer list item", exception); }
                    }
                    if (!validator.test(item)) { throw new IllegalArgumentException("Invalid list item"); }
                    normalized.add(item);
                }
                return normalized;
            }
            throw new IllegalArgumentException("Wrong type or range for " + String.join(".", path));
        }
    }

    private final Map<String, Option> options;
    private FabricConfigSpec(final Map<String, Option> options) { this.options = new LinkedHashMap<>(options); }
    public Map<String, Option> options() { return java.util.Collections.unmodifiableMap(options); }

    public Map<String, Object> snapshot()
    {
        final Map<String, Object> result = new LinkedHashMap<>();
        options.forEach((key, option) -> result.put(key, serializable(option.value().get())));
        return result;
    }

    public Map<String, Object> validateSnapshot(final Map<String, ?> snapshot)
    {
        final Map<String, Object> result = new LinkedHashMap<>();
        options.forEach((key, option) -> result.put(key, option.normalize(snapshot.get(key))));
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void applySnapshot(final Map<String, ?> snapshot)
    {
        // Validate all values before changing any; invalid remote data cannot partially override the client.
        final Map<String, Object> validated = validateSnapshot(snapshot);
        validated.forEach((key, value) -> ((ConfigValue) options.get(key).value()).set(value));
    }

    static Object serializable(final Object value)
    {
        if (value instanceof Enum<?> enumeration) { return enumeration.name(); }
        return value instanceof List<?> list ? new ArrayList<>(list) : value;
    }

    public static class ConfigValue<T>
    {
        private volatile T value;

        public ConfigValue(final T value)
        {
            this.value = value;
        }

        public T get()
        {
            return value;
        }

        public boolean accepts(final Object candidate) { return true; }

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
            if (!Double.isFinite(value)) { throw new IllegalArgumentException("Non-finite configuration value"); }
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
        private final Map<String, Option> options = new LinkedHashMap<>();
        private String comment = "";

        public FabricConfigSpec build() { return new FabricConfigSpec(options); }
        public <T extends ConfigValue<?>> T defineValue(final String key, final T value, final Number min,
                                                         final Number max, final Predicate<Object> validator)
        { return register(key, value, min, max, validator); }

        private <T extends ConfigValue<?>> T register(final String key, final T value, final Number min,
                                                       final Number max, final Predicate<Object> validator)
        {
            final List<String> path = new ArrayList<>();
            categories.descendingIterator().forEachRemaining(path::add);
            path.add(key);
            final String fullKey = String.join(".", path);
            final Object defaultValue = value.get() instanceof List<?> list ? List.copyOf(list) : value.get();
            final String description = comment + "\nDefault: " + serializable(defaultValue)
                + (min == null ? "" : "\nRange: " + min + " to " + max);
            final Option option = new Option(List.copyOf(path), value, defaultValue, description, min, max,
                validator == null ? ignored -> true : validator);
            if (options.putIfAbsent(fullKey, option) != null) { throw new IllegalArgumentException("Duplicate option " + fullKey); }
            comment = "";
            return value;
        }

        public Builder comment(final String text)
        {
            comment = text;
            return this;
        }

        public Builder translation(final String ignored)
        {
            return this;
        }

        public Builder push(final String category)
        {
            categories.push(category);
            comment = "";
            return this;
        }

        public Builder pop()
        {
            if (!categories.isEmpty()) categories.pop();
            return this;
        }

        public BooleanValue define(final String key, final boolean value)
        {
            return register(key, new BooleanValue(value), null, null, null);
        }

        public IntValue define(final String key, final int value)
        {
            return defineInRange(key, value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public LongValue define(final String key, final long value)
        {
            return defineInRange(key, value, Long.MIN_VALUE, Long.MAX_VALUE);
        }

        public DoubleValue define(final String key, final double value)
        {
            return defineInRange(key, value, -Double.MAX_VALUE, Double.MAX_VALUE);
        }

        public IntValue defineInRange(final String key, final int value, final int min, final int max)
        {
            return register(key, new IntValue(value, min, max), min, max, null);
        }

        public LongValue defineInRange(final String key, final long value, final long min, final long max)
        {
            return register(key, new LongValue(value, min, max), min, max, null);
        }

        public DoubleValue defineInRange(final String key, final double value, final double min, final double max)
        {
            return register(key, new DoubleValue(value, min, max), min, max, null);
        }

        public <T> ConfigValue<List<? extends T>> defineList(final String key, final List<? extends T> values, final Predicate<Object> validator)
        {
            final ArrayList<T> copy = new ArrayList<>();
            for (final T value : values)
            {
                if (validator == null || validator.test(value)) copy.add(value);
            }
            return register(key, new ConfigValue<List<? extends T>>(copy), null, null, validator);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(final String key, final V value)
        {
            return register(key, new EnumValue<>(Objects.requireNonNull(value)), null, null, null);
        }
    }
}
