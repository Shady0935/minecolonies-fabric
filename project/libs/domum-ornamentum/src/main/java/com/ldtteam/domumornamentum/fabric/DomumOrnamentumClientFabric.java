package com.ldtteam.domumornamentum.fabric;

import com.ldtteam.domumornamentum.client.event.handlers.ModBusEventHandler;
import com.ldtteam.domumornamentum.client.event.handlers.MateriallyTexturedBlockPreviewRenderHandler;
import com.ldtteam.domumornamentum.client.event.handlers.RegisterColorHandlersEventHandler;
import com.ldtteam.domumornamentum.client.model.loader.MateriallyTexturedModelLoader;
import net.fabricmc.api.ClientModInitializer;

/** Fabric client bootstrap for Domum Ornamentum render/color hooks. */
public final class DomumOrnamentumClientFabric implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ModBusEventHandler.registerClient();
        RegisterColorHandlersEventHandler.registerClient();
        MateriallyTexturedModelLoader.registerClient();
        MateriallyTexturedBlockPreviewRenderHandler.registerClient();
    }
}
