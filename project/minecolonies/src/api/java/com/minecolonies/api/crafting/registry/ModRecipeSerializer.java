package com.minecolonies.api.crafting.registry;

import com.minecolonies.api.crafting.CompostRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import com.minecolonies.fabric.registry.FabricRegistryObject;

/**
 * Holds ref to the mod recipe serializers and recipe types.
 */
public class ModRecipeSerializer
{
    public static FabricRegistryObject<CompostRecipe.Serializer> CompostRecipeSerializer;
    public static FabricRegistryObject<RecipeType<CompostRecipe>>   CompostRecipeType;
}
