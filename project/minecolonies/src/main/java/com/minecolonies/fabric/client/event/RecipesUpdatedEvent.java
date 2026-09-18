package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.item.crafting.RecipeManager;

/** Client recipe-sync bridge. */
public final class RecipesUpdatedEvent extends Event
{
    private final RecipeManager recipeManager;

    public RecipesUpdatedEvent(final RecipeManager recipeManager)
    {
        this.recipeManager = recipeManager;
    }

    public RecipeManager getRecipeManager()
    {
        return recipeManager;
    }
}
