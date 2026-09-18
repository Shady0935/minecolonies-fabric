package com.minecolonies.coremod.recipes;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.fabric.registry.FabricRegistries;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

/** Dynamic custom ingredient matching edible items with optional food bounds. */
public final class FoodIngredient implements CustomIngredient
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "food");
    public static final String MIN_HEALING_PROP = "min-healing";
    public static final String MAX_HEALING_PROP = "max-healing";
    public static final String MIN_SATURATION_PROP = "min-saturation";
    public static final String MAX_SATURATION_PROP = "max-saturation";
    public static final CustomIngredientSerializer<FoodIngredient> SERIALIZER = new Serializer();

    private final Optional<Integer> minHealing;
    private final Optional<Integer> maxHealing;
    private final Optional<Float> minSaturation;
    private final Optional<Float> maxSaturation;

    private FoodIngredient(final Builder builder)
    {
        minHealing = builder.minHealing;
        maxHealing = builder.maxHealing;
        minSaturation = builder.minSaturation;
        maxSaturation = builder.maxSaturation;
    }

    private boolean matchesFood(final ItemStack stack)
    {
        final FoodProperties food = stack.getItem().getFoodProperties();
        return food != null
          && minHealing.map(value -> food.getNutrition() >= value).orElse(true)
          && maxHealing.map(value -> food.getNutrition() < value).orElse(true)
          && minSaturation.map(value -> food.getSaturationModifier() >= value).orElse(true)
          && maxSaturation.map(value -> food.getSaturationModifier() < value).orElse(true);
    }

    @Override
    public boolean test(final ItemStack stack)
    {
        return stack != null && ItemStackUtils.ISFOOD.test(stack) && matchesFood(stack);
    }

    @Override
    public List<ItemStack> getMatchingStacks()
    {
        return FabricRegistries.ITEMS.getValues().stream().map(ItemStack::new).filter(this::test).toList();
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

    public static final class Builder
    {
        private Optional<Integer> minHealing = Optional.empty();
        private Optional<Integer> maxHealing = Optional.empty();
        private Optional<Float> minSaturation = Optional.empty();
        private Optional<Float> maxSaturation = Optional.empty();

        public Builder minHealing(final int healing) { minHealing = Optional.of(healing); return this; }
        public Builder maxHealing(final int healing) { maxHealing = Optional.of(healing); return this; }
        public Builder minSaturation(final float saturation) { minSaturation = Optional.of(saturation); return this; }
        public Builder maxSaturation(final float saturation) { maxSaturation = Optional.of(saturation); return this; }

        public Ingredient build()
        {
            return new FoodIngredient(this).toVanilla();
        }
    }

    public static final class Serializer implements CustomIngredientSerializer<FoodIngredient>
    {
        @Override
        public ResourceLocation getIdentifier()
        {
            return ID;
        }

        @Override
        public FoodIngredient read(final JsonObject json)
        {
            final Builder builder = new Builder();
            if (json.has(MIN_HEALING_PROP)) builder.minHealing(GsonHelper.getAsInt(json, MIN_HEALING_PROP));
            if (json.has(MAX_HEALING_PROP)) builder.maxHealing(GsonHelper.getAsInt(json, MAX_HEALING_PROP));
            if (json.has(MIN_SATURATION_PROP)) builder.minSaturation(GsonHelper.getAsFloat(json, MIN_SATURATION_PROP));
            if (json.has(MAX_SATURATION_PROP)) builder.maxSaturation(GsonHelper.getAsFloat(json, MAX_SATURATION_PROP));
            return new FoodIngredient(builder);
        }

        @Override
        public void write(final JsonObject json, final FoodIngredient ingredient)
        {
            json.addProperty("fabric:type", ID.toString());
            ingredient.minHealing.ifPresent(value -> json.addProperty(MIN_HEALING_PROP, value));
            ingredient.maxHealing.ifPresent(value -> json.addProperty(MAX_HEALING_PROP, value));
            ingredient.minSaturation.ifPresent(value -> json.addProperty(MIN_SATURATION_PROP, value));
            ingredient.maxSaturation.ifPresent(value -> json.addProperty(MAX_SATURATION_PROP, value));
        }

        @Override
        public FoodIngredient read(final FriendlyByteBuf buffer)
        {
            final Builder builder = new Builder();
            final int flags = buffer.readVarInt();
            if ((flags & 1) != 0) builder.minHealing(buffer.readVarInt());
            if ((flags & 2) != 0) builder.maxHealing(buffer.readVarInt());
            if ((flags & 4) != 0) builder.minSaturation(buffer.readFloat());
            if ((flags & 8) != 0) builder.maxSaturation(buffer.readFloat());
            return new FoodIngredient(builder);
        }

        @Override
        public void write(final FriendlyByteBuf buffer, final FoodIngredient ingredient)
        {
            buffer.writeVarInt((ingredient.minHealing.isPresent() ? 1 : 0)
              | (ingredient.maxHealing.isPresent() ? 2 : 0)
              | (ingredient.minSaturation.isPresent() ? 4 : 0)
              | (ingredient.maxSaturation.isPresent() ? 8 : 0));
            ingredient.minHealing.ifPresent(buffer::writeVarInt);
            ingredient.maxHealing.ifPresent(buffer::writeVarInt);
            ingredient.minSaturation.ifPresent(buffer::writeFloat);
            ingredient.maxSaturation.ifPresent(buffer::writeFloat);
        }
    }
}
