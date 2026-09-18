package com.ldtteam.domumornamentum.client.model.properties;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
public class ModProperties
{

    private ModProperties()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModProperties. This is a utility class");
    }

    /**
     * Fabric's 1.20 renderer does not expose Forge's per-block ModelData channel. The
     * material payload is read from the block entity/item NBT by the Fabric model hook.
     */
    public static final Object MATERIAL_TEXTURE_PROPERTY = new Object();
}
