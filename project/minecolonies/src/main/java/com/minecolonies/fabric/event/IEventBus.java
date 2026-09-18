package com.minecolonies.fabric.event;

import com.minecolonies.fabric.common.MinecraftForge;

import java.util.function.Consumer;

/**
 * Small bridge for the parts of the upstream source that still describe
 * registration through Forge event buses.  Fabric entrypoints use the same
 * bus while the remaining listeners are being moved to Fabric callbacks.
 */
public final class IEventBus
{
    public static final IEventBus FORGE = new IEventBus(MinecraftForge.EVENT_BUS);
    public static final IEventBus MOD = new IEventBus(MinecraftForge.EVENT_BUS);

    private final MinecraftForge.EventBus delegate;

    private IEventBus(final MinecraftForge.EventBus delegate)
    {
        this.delegate = delegate;
    }

    public void register(final Object listener)
    {
        delegate.register(listener);
    }

    public <T> void addListener(final Consumer<T> listener)
    {
        delegate.addListener(listener);
    }
}
