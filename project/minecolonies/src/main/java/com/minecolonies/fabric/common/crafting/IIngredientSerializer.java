package com.minecolonies.fabric.common.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

public interface IIngredientSerializer<T extends Ingredient>
{
    T parse(JsonObject json);
    T parse(FriendlyByteBuf buffer);
    void write(FriendlyByteBuf buffer, T ingredient);
    void write(JsonObject json, T ingredient);
}
