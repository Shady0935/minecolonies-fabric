package com.minecolonies.fabric.client;

import com.minecolonies.fabric.client.event.RegisterRecipeBookCategoriesEvent;
import com.minecolonies.fabric.common.MinecraftForge;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.Map;
import java.util.function.Function;

/** Apply the category finders registered by the original mod to Fabric's recipe-book lookup. */
public final class ClientRecipeCategories
{
    private static Map<RecipeType<?>, Function<?, RecipeBookCategories>> finders = Map.of();
    private ClientRecipeCategories() { }
    public static void register()
    {
        final RegisterRecipeBookCategoriesEvent event = new RegisterRecipeBookCategoriesEvent();
        MinecraftForge.EVENT_BUS.post(event);
        finders = event.getFinders();
    }
    @SuppressWarnings("unchecked")
    public static RecipeBookCategories find(final Recipe<?> recipe)
    {
        final Function<Recipe<?>, RecipeBookCategories> finder = (Function<Recipe<?>, RecipeBookCategories>) finders.get(recipe.getType());
        return finder == null ? null : finder.apply(recipe);
    }
}
