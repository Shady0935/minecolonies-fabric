package com.ldtteam.structurize.client.model;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * Parses the legacy overlay model declaration for tooling and compatibility.
 * Fabric's vanilla model loader ignores the old Forge {@code loader} key.
 */
public final class OverlaidModelLoader
{
    private OverlaidModelLoader()
    {
    }

    public static OverlaidGeometry read(final JsonObject jsonObject)
    {
        return new OverlaidGeometry(new ResourceLocation(jsonObject.get("parent").getAsString()));
    }
}
