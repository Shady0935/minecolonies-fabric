package com.ldtteam.domumornamentum.recipe;

import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipeSerializer;
import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

/** Fabric recipe serializer registrations for Domum Ornamentum. */
public final class ModRecipeSerializers
{
    public static final Supplier<RecipeSerializer<ArchitectsCutterRecipe>> ARCHITECTS_CUTTER = register(
        "architects_cutter", new ArchitectsCutterRecipeSerializer());

    private ModRecipeSerializers()
    {
    }

    public static void init()
    {
        // Static registration is deliberately eager; this method documents the init order.
    }

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> Supplier<RecipeSerializer<T>> register(
        final String name, final RecipeSerializer<T> serializer)
    {
        final ResourceLocation id = new ResourceLocation(Constants.MOD_ID, name);
        final RecipeSerializer<T> value = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer);
        return () -> value;
    }
}
