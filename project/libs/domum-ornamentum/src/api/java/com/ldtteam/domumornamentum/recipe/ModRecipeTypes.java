package com.ldtteam.domumornamentum.recipe;

import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

/** Fabric recipe type registrations for Domum Ornamentum. */
public final class ModRecipeTypes
{
    public static final Supplier<RecipeType<ArchitectsCutterRecipe>> ARCHITECTS_CUTTER = register("architects_cutter");

    private ModRecipeTypes()
    {
    }

    public static void init()
    {
        // Static registration is deliberately eager; this method documents the init order.
    }

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> Supplier<RecipeType<T>> register(final String name)
    {
        final ResourceLocation id = new ResourceLocation(Constants.MOD_ID, name);
        final RecipeType<T> value = Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>()
        {
            @Override
            public String toString()
            {
                return id.toString();
            }
        });
        return () -> value;
    }
}
