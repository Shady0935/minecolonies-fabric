package com.ldtteam.domumornamentum.client.event.handlers;

import com.ldtteam.domumornamentum.block.ModBlocks;
import com.ldtteam.domumornamentum.client.color.MateriallyTexturedBlockBlockColor;
import com.ldtteam.domumornamentum.client.color.MateriallyTexturedBlockItemColor;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

/** Fabric block and item tint registration. */
public final class RegisterColorHandlersEventHandler
{
    private RegisterColorHandlersEventHandler()
    {
    }

    public static void registerClient()
    {
        ColorProviderRegistry.ITEM.register(new MateriallyTexturedBlockItemColor(), ModBlocks.getMateriallyTexturableItems());
        ColorProviderRegistry.BLOCK.register(new MateriallyTexturedBlockBlockColor(), ModBlocks.getMateriallyTexturableBlocks());
    }
}
