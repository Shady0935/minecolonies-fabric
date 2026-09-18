package com.ldtteam.domumornamentum.fabric;

import com.ldtteam.domumornamentum.DomumOrnamentum;
import net.fabricmc.api.ModInitializer;

/** Fabric bootstrap for common Domum Ornamentum registration. */
public final class DomumOrnamentumFabric implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        new DomumOrnamentum().onInitialize();
    }
}
