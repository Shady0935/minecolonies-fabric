package com.ldtteam.domumornamentum.client.model.loader;

import com.ldtteam.domumornamentum.client.model.baked.MateriallyTexturedBakedModel;
import com.ldtteam.domumornamentum.util.Constants;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.resources.model.BakedModel;

/** Fabric model-loading replacement for Forge's geometry-loader registration. */
public final class MateriallyTexturedModelLoader
{
    private static boolean registered;

    private MateriallyTexturedModelLoader()
    {
    }

    public static synchronized void registerClient()
    {
        if (registered) return;
        registered = true;

        ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register((model, bakeContext) -> {
            if (!(model instanceof BakedModel) || model instanceof MateriallyTexturedBakedModel)
            {
                return model;
            }

            final String namespace = bakeContext.id().getNamespace();
            final String path = bakeContext.id().getPath();
            if (Constants.MOD_ID.equals(namespace) && (path.startsWith("block/") || path.startsWith("item/")))
            {
                return new MateriallyTexturedBakedModel(model);
            }
            return model;
        }));
    }
}
