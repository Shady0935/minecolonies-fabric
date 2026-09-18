package com.minecolonies.fabric.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceLocation;

public final class ForgeHooks
{
    private ForgeHooks()
    {
    }

    public static Component newChatWithLinks(final String text)
    {
        return Component.literal(text);
    }

    public static LootTable loadLootTable(final Gson gson, final ResourceLocation location, final JsonObject json, final boolean custom)
    {
        return gson.fromJson(json, LootTable.class);
    }
}
