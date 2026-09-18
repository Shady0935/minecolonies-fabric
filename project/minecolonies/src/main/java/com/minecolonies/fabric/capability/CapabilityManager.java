package com.minecolonies.fabric.capability;

public final class CapabilityManager
{
    private CapabilityManager()
    {
    }

    public static <T> Capability<T> get(final CapabilityToken<T> token)
    {
        return new Capability<>();
    }
}
