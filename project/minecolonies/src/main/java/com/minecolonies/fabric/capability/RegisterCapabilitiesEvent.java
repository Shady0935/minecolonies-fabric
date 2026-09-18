package com.minecolonies.fabric.capability;

/** Compatibility event; Fabric capability attachment is handled by the owning provider. */
public final class RegisterCapabilitiesEvent
{
    public <T> void register(final Class<T> type)
    {
    }
}
