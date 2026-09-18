package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.event.Event;

/** Client connection lifecycle events used by the upstream handlers. */
public final class ClientPlayerNetworkEvent
{
    private ClientPlayerNetworkEvent() { }

    public static final class LoggingOut extends Event
    {
    }
}
