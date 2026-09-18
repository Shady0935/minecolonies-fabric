package com.minecolonies.fabric.common.brewing;

import net.minecraft.world.item.ItemStack;

public interface IBrewingRecipe
{
    boolean isInput(ItemStack stack);
    boolean isIngredient(ItemStack stack);
    ItemStack getOutput(ItemStack input, ItemStack ingredient);
}
