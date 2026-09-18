package com.minecolonies.fabric.capability;

import com.minecolonies.fabric.util.LazyOptional;

/** Small capability identity used by the Fabric compatibility layer. */
public final class Capability<T>
{
    public <U> LazyOptional<U> orEmpty(final Capability<U> requested, final LazyOptional<U> value)
    {
        return this == requested ? value : LazyOptional.empty();
    }
}
