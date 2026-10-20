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
        final net.minecraft.network.chat.MutableComponent result = Component.empty();
        final java.util.regex.Matcher links = java.util.regex.Pattern.compile("https?://[^\\s<>]+").matcher(text);
        int position = 0;
        while (links.find())
        {
            result.append(Component.literal(text.substring(position, links.start())));
            final String url = links.group();
            result.append(Component.literal(url).withStyle(net.minecraft.ChatFormatting.BLUE, net.minecraft.ChatFormatting.UNDERLINE)
                .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_URL, url))));
            position = links.end();
        }
        return result.append(Component.literal(text.substring(position)));
    }

    public static LootTable loadLootTable(final Gson gson, final ResourceLocation location, final JsonObject json, final boolean custom)
    {
        return gson.fromJson(json, LootTable.class);
    }
}
