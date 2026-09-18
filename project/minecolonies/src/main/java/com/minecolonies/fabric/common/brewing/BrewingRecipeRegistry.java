package com.minecolonies.fabric.common.brewing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Vanilla-compatible brewing lookup used by MineColonies crafting screens. */
public final class BrewingRecipeRegistry
{
    private static final List<IBrewingRecipe> RECIPES = new ArrayList<>();

    private BrewingRecipeRegistry()
    {
    }

    public static List<IBrewingRecipe> getRecipes()
    {
        return List.copyOf(RECIPES);
    }

    public static boolean isValidIngredient(final ItemStack stack)
    {
        return stack != null && !stack.isEmpty() && (stack.is(Items.NETHER_WART) || stack.is(Items.REDSTONE)
          || stack.is(Items.GLOWSTONE_DUST) || stack.is(Items.GUNPOWDER) || stack.is(Items.DRAGON_BREATH)
          || stack.is(Items.FERMENTED_SPIDER_EYE) || stack.is(Items.SUGAR) || stack.is(Items.RABBIT_FOOT)
          || stack.is(Items.GLISTERING_MELON_SLICE) || stack.is(Items.SPIDER_EYE) || stack.is(Items.PUFFERFISH)
          || stack.is(Items.MAGMA_CREAM) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.BLAZE_POWDER));
    }

    public static boolean isValidInput(final ItemStack stack)
    {
        return stack != null && !stack.isEmpty() && (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION));
    }

    public static ItemStack getOutput(final ItemStack ingredient, final ItemStack input)
    {
        for (IBrewingRecipe recipe : RECIPES)
        {
            if (recipe.isInput(input) && recipe.isIngredient(ingredient)) return recipe.getOutput(input, ingredient);
        }
        return ItemStack.EMPTY;
    }
}
