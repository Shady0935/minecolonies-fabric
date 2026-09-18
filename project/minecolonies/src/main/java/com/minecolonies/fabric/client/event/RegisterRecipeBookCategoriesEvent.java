package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.event.Event;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Recipe-book category bridge. */
public final class RegisterRecipeBookCategoriesEvent extends Event
{
    private final Map<RecipeType<?>, Function<?, RecipeBookCategories>> finders = new LinkedHashMap<>();

    public <T extends Recipe<?>> void registerRecipeCategoryFinder(final RecipeType<T> type, final Function<T, RecipeBookCategories> finder)
    {
        finders.put(type, finder);
    }

    public Map<RecipeType<?>, Function<?, RecipeBookCategories>> getFinders()
    {
        return Map.copyOf(finders);
    }
}
