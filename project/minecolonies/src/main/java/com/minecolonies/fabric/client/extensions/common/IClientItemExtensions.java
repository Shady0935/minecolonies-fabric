package com.minecolonies.fabric.client.extensions.common;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

/** Minimal source-compatible client extension for custom item renderers. */
public interface IClientItemExtensions
{
    default BlockEntityWithoutLevelRenderer getCustomRenderer()
    {
        return null;
    }
}
