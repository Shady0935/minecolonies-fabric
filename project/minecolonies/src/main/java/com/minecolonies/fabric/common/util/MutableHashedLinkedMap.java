package com.minecolonies.fabric.common.util;

import java.util.LinkedHashMap;
/** Ordered map with the merge callback shape used by creative-tab generation. */
public class MutableHashedLinkedMap<K, V> extends LinkedHashMap<K, V>
{
    @FunctionalInterface
    public interface MergeFunction<K, V>
    {
        V apply(K key, V left, V right);
    }

    private final MergeFunction<K, V> mergeFunction;

    public MutableHashedLinkedMap(final Object ignoredStrategy, final MergeFunction<K, V> mergeFunction)
    {
        this.mergeFunction = mergeFunction;
    }

    @Override
    public V put(final K key, final V value)
    {
        if (containsKey(key))
        {
            return super.put(key, mergeFunction.apply(key, get(key), value));
        }
        return super.put(key, value);
    }
}
