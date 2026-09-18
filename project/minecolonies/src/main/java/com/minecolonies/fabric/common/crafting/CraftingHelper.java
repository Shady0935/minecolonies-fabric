package com.minecolonies.fabric.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public final class CraftingHelper
{
    private CraftingHelper()
    {
    }

    public static Ingredient getIngredient(final JsonObject json, final boolean allowEmpty)
    {
        return Ingredient.fromJson(json);
    }

    public static ResourceLocation getID(final Object serializer)
    {
        return new ResourceLocation("minecolonies", serializer.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT));
    }

    public static void register(final ResourceLocation id, final CustomIngredientSerializer<?> serializer)
    {
        CustomIngredientSerializer.register(serializer);
    }
}
