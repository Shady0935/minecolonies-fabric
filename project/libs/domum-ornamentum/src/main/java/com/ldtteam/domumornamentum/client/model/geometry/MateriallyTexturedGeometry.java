package com.ldtteam.domumornamentum.client.model.geometry;

import net.minecraft.resources.ResourceLocation;

/**
 * Retained data holder for the Forge JSON geometry name. Fabric 1.20 applies the equivalent
 * behavior with a post-bake model modifier, see {@code MateriallyTexturedModelLoader}.
 */
public final class MateriallyTexturedGeometry
{
    private final ResourceLocation innerModelLocation;

    public MateriallyTexturedGeometry(final ResourceLocation innerModelLocation)
    {
        this.innerModelLocation = innerModelLocation;
    }

    public ResourceLocation innerModelLocation()
    {
        return innerModelLocation;
    }
}
