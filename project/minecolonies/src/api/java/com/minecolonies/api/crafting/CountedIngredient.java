package com.minecolonies.api.crafting;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Fabric custom ingredient that keeps the required item count for recipe
 * displays while deliberately matching a single stack independently of its
 * count. The compost barrel consumes across multiple inventory slots.
 */
public final class CountedIngredient implements CustomIngredient
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "counted");
    public static final CustomIngredientSerializer<CountedIngredient> SERIALIZER = new Serializer();

    @NotNull
    private final Ingredient child;
    private final int count;

    public CountedIngredient(@NotNull final Ingredient child, final int count)
    {
        if (child.isEmpty() || count <= 0)
        {
            throw new IllegalArgumentException("Counted ingredient must have a non-empty child and positive count");
        }
        this.child = child;
        this.count = count;
    }

    @NotNull
    public Ingredient getChild()
    {
        return child;
    }

    public int getCount()
    {
        return count;
    }

    public static Ingredient of(@NotNull final Ingredient child, final int count)
    {
        return count == 1 ? child : new CountedIngredient(child, count).toVanilla();
    }

    @Override
    public boolean test(final ItemStack stack)
    {
        return stack != null && child.test(stack);
    }

    @Override
    public List<ItemStack> getMatchingStacks()
    {
        return Arrays.stream(child.getItems()).map(ItemStack::copy).peek(stack -> stack.setCount(count)).toList();
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

    public static final class Serializer implements CustomIngredientSerializer<CountedIngredient>
    {
        @Override
        public ResourceLocation getIdentifier()
        {
            return ID;
        }

        @Override
        public CountedIngredient read(final JsonObject json)
        {
            return new CountedIngredient(Ingredient.fromJson(json.get("item")), GsonHelper.getAsInt(json, "count", 1));
        }

        @Override
        public void write(final JsonObject json, final CountedIngredient ingredient)
        {
            json.addProperty("fabric:type", ID.toString());
            json.add("item", ingredient.child.toJson());
            if (ingredient.count > 1) json.addProperty("count", ingredient.count);
        }

        @Override
        public CountedIngredient read(final FriendlyByteBuf buffer)
        {
            final int count = buffer.readVarInt();
            return new CountedIngredient(Ingredient.fromNetwork(buffer), count);
        }

        @Override
        public void write(final FriendlyByteBuf buffer, final CountedIngredient ingredient)
        {
            buffer.writeVarInt(ingredient.count);
            ingredient.child.toNetwork(buffer);
        }
    }
}
