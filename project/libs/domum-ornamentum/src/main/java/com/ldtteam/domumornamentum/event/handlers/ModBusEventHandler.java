package com.ldtteam.domumornamentum.event.handlers;

import com.ldtteam.domumornamentum.Network;

/** Common Fabric lifecycle hooks replacing Forge's mod event bus subscriber. */
public final class ModBusEventHandler
{
    private ModBusEventHandler()
    {
    }

    public static void registerCommon()
    {
        Network.getNetwork().registerMessages();
    }
}
