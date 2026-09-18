package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.event.Event;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.ArrayList;
import java.util.List;

/** Client resource reload registration bridge. */
public final class RegisterClientReloadListenersEvent extends Event
{
    private final List<PreparableReloadListener> listeners = new ArrayList<>();

    public void registerReloadListener(final PreparableReloadListener listener)
    {
        listeners.add(listener);
    }

    public List<PreparableReloadListener> getListeners()
    {
        return List.copyOf(listeners);
    }
}
