package com.ldtteam.domumornamentum.client.model.loader;

import com.ldtteam.domumornamentum.client.model.baked.MateriallyTexturedBakedModel;
import com.ldtteam.domumornamentum.item.interfaces.IDoItem;
import com.ldtteam.domumornamentum.util.Constants;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

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

        ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, bakeContext) -> {
            if (!(model instanceof BakedModel) || model instanceof MateriallyTexturedBakedModel)
            {
                return model;
            }

            final String namespace = bakeContext.id().getNamespace();
            final String path = bakeContext.id().getPath();
            // Fabric supplies mod:item_name#inventory, not mod:item/item_name, for inventory models.
            // The top-level model must receive the wrapper to read each stack's textureData.
            final boolean materialInventory = bakeContext.id() instanceof ModelResourceLocation modelId
                && "inventory".equals(modelId.getVariant())
                && BuiltInRegistries.ITEM.get(new ResourceLocation(namespace, path)) instanceof IDoItem;
            if (Constants.MOD_ID.equals(namespace)
                  && (materialInventory || path.startsWith("block/") || path.startsWith("item/")))
            {
                return new MateriallyTexturedBakedModel(model);
            }
            return model;
        }));
    }
}
