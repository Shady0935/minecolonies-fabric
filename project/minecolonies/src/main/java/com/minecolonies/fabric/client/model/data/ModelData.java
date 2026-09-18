package com.minecolonies.fabric.client.model.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Small Fabric-side replacement for Forge's model data container.
 *
 * <p>MineColonies uses this container for block-entity render hints.  The
 * Fabric renderer does not consume Forge model data, but retaining the value
 * object keeps the API stable while the corresponding Fabric model hooks are
 * wired in.</p>
 */
public final class ModelData
{
    public static final ModelData EMPTY = new ModelData(Collections.emptyMap());

    private final Map<Object, Object> values;

    private ModelData(final Map<Object, Object> values)
    {
        this.values = values;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final Object property)
    {
        return (T) values.get(property);
    }

    public boolean has(final Object property)
    {
        return values.containsKey(property);
    }

    public static final class Builder
    {
        private final Map<Object, Object> values = new HashMap<>();

        public <T> Builder with(final Object property, final T value)
        {
            values.put(property, value);
            return this;
        }

        public ModelData build()
        {
            return new ModelData(Collections.unmodifiableMap(new HashMap<>(values)));
        }
    }
}
