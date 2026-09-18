package com.minecolonies.fabric.capability;

import com.minecolonies.fabric.util.LazyOptional;
import net.minecraft.core.Direction;

public interface ICapabilityProvider
{
    <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side);

    default <T> LazyOptional<T> getCapability(final Capability<T> capability)
    {
        return getCapability(capability, null);
    }
}
