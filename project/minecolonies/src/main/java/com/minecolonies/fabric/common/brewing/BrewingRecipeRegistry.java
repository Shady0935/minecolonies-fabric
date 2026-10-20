package com.minecolonies.fabric.common.brewing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Vanilla-compatible brewing lookup used by MineColonies crafting screens. */
public final class BrewingRecipeRegistry
{
    private static final List<IBrewingRecipe> RECIPES = new ArrayList<>();
    private static final IBrewingRecipe VANILLA = new IBrewingRecipe()
    {
        @Override public boolean isInput(final ItemStack stack) { return potionInput(stack); }
        @Override public boolean isIngredient(final ItemStack stack) { return PotionBrewing.isIngredient(stack); }
        @Override public ItemStack getOutput(final ItemStack input, final ItemStack ingredient)
        { return PotionBrewing.hasMix(input, ingredient) ? PotionBrewing.mix(ingredient, input) : ItemStack.EMPTY; }
    };

    private BrewingRecipeRegistry()
    {
    }

    public static List<IBrewingRecipe> getRecipes()
    {
        final List<IBrewingRecipe> recipes = new ArrayList<>(RECIPES);
        recipes.add(VANILLA);
        return List.copyOf(recipes);
    }

    public static void addRecipe(final IBrewingRecipe recipe) { RECIPES.add(java.util.Objects.requireNonNull(recipe)); }

    public static boolean isValidIngredient(final ItemStack stack)
    {
        return stack != null && !stack.isEmpty() && getRecipes().stream().anyMatch(recipe -> recipe.isIngredient(stack));
    }

    public static boolean isValidInput(final ItemStack stack)
    {
        return stack != null && !stack.isEmpty() && getRecipes().stream().anyMatch(recipe -> recipe.isInput(stack));
    }

    private static boolean potionInput(final ItemStack stack)
    { return stack != null && !stack.isEmpty() && (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)); }

    public static ItemStack getOutput(final ItemStack ingredient, final ItemStack input)
    {
        for (IBrewingRecipe recipe : getRecipes())
        {
            if (recipe.isInput(input) && recipe.isIngredient(ingredient))
            {
                final ItemStack output = recipe.getOutput(input, ingredient);
                if (!output.isEmpty()) { return output; }
            }
        }
        return ItemStack.EMPTY;
    }
}
