package com.minecolonies.coremod.recipes;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.fabric.registry.FabricRegistries;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;

import java.util.List;

/** Custom ingredient matching all registered crop and stem block items. */
public final class PlantIngredient implements CustomIngredient
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "plant");
    public static final CustomIngredientSerializer<PlantIngredient> SERIALIZER = new Serializer();

    private static volatile PlantIngredient instance;
    private final List<ItemStack> matchingStacks;

    private PlantIngredient()
    {
        matchingStacks = FabricRegistries.ITEMS.getValues().stream()
          .filter(item -> item instanceof BlockItem blockItem
            && (blockItem.getBlock() instanceof CropBlock || blockItem.getBlock() instanceof StemBlock))
          .map(ItemStack::new)
          .toList();
    }

    public static PlantIngredient getInstance()
    {
        PlantIngredient result = instance;
        if (result == null)
        {
            synchronized (PlantIngredient.class)
            {
                result = instance;
                if (result == null) instance = result = new PlantIngredient();
            }
        }
        return result;
    }

    public static Ingredient of()
    {
        return getInstance().toVanilla();
    }

    @Override
    public boolean test(final ItemStack stack)
    {
        return stack != null && matchingStacks.stream().anyMatch(candidate -> candidate.getItem() == stack.getItem());
    }

    @Override
    public List<ItemStack> getMatchingStacks()
    {
        return matchingStacks.stream().map(ItemStack::copy).toList();
    }

    @Override
    public boolean requiresTesting()
    {
        return false;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    public static final class Serializer implements CustomIngredientSerializer<PlantIngredient>
    {
        @Override
        public ResourceLocation getIdentifier()
        {
            return ID;
        }

        @Override
        public PlantIngredient read(final com.google.gson.JsonObject json)
        {
            return getInstance();
        }

        @Override
        public void write(final com.google.gson.JsonObject json, final PlantIngredient ingredient)
        {
            json.addProperty("fabric:type", ID.toString());
        }

        @Override
        public PlantIngredient read(final FriendlyByteBuf buffer)
        {
            return getInstance();
        }

        @Override
        public void write(final FriendlyByteBuf buffer, final PlantIngredient ingredient)
        {
        }
    }
}
