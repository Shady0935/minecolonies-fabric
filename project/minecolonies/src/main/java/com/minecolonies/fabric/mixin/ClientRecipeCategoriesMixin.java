package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.client.ClientRecipeCategories;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeCategoriesMixin
{
    @Inject(method = "getCategory", at = @At("HEAD"), cancellable = true)
    private static void minecolonies$recipeCategory(final Recipe<?> recipe,
                                                    final CallbackInfoReturnable<RecipeBookCategories> callback)
    {
        final RecipeBookCategories category = ClientRecipeCategories.find(recipe);
        if (category != null) { callback.setReturnValue(category); }
    }
}
