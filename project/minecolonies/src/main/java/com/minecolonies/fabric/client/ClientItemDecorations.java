package com.minecolonies.fabric.client;

import com.minecolonies.fabric.client.event.RegisterItemDecorationsEvent;
import com.minecolonies.fabric.common.MinecraftForge;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Map;

/** Consume the retained registration event through the vanilla GUI rendering callback. */
public final class ClientItemDecorations
{
    private static Map<Item, IItemDecorator> decorators = Map.of();
    private ClientItemDecorations() { }
    public static void register()
    {
        final RegisterItemDecorationsEvent event = new RegisterItemDecorationsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        decorators = event.getDecorators();
    }
    public static void render(final GuiGraphics graphics, final Font font, final ItemStack stack, final int x, final int y)
    {
        final IItemDecorator decorator = decorators.get(stack.getItem());
        if (decorator != null && net.minecraft.client.Minecraft.getInstance().level != null)
        { decorator.render(graphics, font, stack, x, y); }
    }
}
