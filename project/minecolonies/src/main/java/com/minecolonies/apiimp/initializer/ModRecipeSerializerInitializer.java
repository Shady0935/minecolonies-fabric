package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.registry.ModRecipeSerializer;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import com.minecolonies.fabric.registry.FabricDeferredRegister;
import com.minecolonies.fabric.registry.FabricRegistries;

public final class ModRecipeSerializerInitializer
{
    public static final FabricDeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = FabricDeferredRegister.create(FabricRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);
    public static final FabricDeferredRegister<RecipeType<?>>       RECIPE_TYPES      = FabricDeferredRegister.create(Registries.RECIPE_TYPE, Constants.MOD_ID);

    static
    {
        ModRecipeSerializer.CompostRecipeSerializer = RECIPE_SERIALIZER.register("composting", CompostRecipe.Serializer::new);
        ModRecipeSerializer.CompostRecipeType = RECIPE_TYPES.register("composting", () -> new RecipeType<CompostRecipe>() { });
    }

    private ModRecipeSerializerInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModRecipeSerializerInitializer but this is a Utility class.");
    }
}
