package com.ldtteam.structurize.client.model;

import net.minecraft.resources.ResourceLocation;

/**
 * Compatibility holder for the former Forge geometry loader.
 */
public final class OverlaidGeometry
{
    private final ResourceLocation overlayModelId;

    public OverlaidGeometry(final ResourceLocation overlayModelId)
    {
        this.overlayModelId = overlayModelId;
    }

    public ResourceLocation getOverlayModelId()
    {
        return overlayModelId;
    }
}
