package com.minecolonies.fabric.event;

import com.minecolonies.fabric.capability.ICapabilityProvider;
import com.minecolonies.fabric.capability.CapabilityHooks;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttachCapabilitiesEvent<T> extends Event
{
    private final T object;
    private final Map<ResourceLocation, ICapabilityProvider> capabilities = new LinkedHashMap<>();

    public AttachCapabilitiesEvent(final T object)
    {
        this.object = object;
    }

    public T getObject()
    {
        return object;
    }

    public void addCapability(final ResourceLocation id, final ICapabilityProvider provider)
    {
        capabilities.put(id, provider);
        CapabilityHooks.attach(object, provider);
    }
}
