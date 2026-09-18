package com.ldtteam.structurize.event;

import com.ldtteam.structurize.util.LanguageHandler;

/** Common lifecycle compatibility hooks for the Fabric port. */
public final class LifecycleSubscriber
{
    private LifecycleSubscriber()
    {
    }

    public static void registerCommon()
    {
        LanguageHandler.setMClanguageLoaded();
    }
}
