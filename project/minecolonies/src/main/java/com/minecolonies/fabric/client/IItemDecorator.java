package com.minecolonies.fabric.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** Fabric equivalent of Forge's item-stack decoration callback. */
@FunctionalInterface
public interface IItemDecorator
{
    boolean render(GuiGraphics graphics, Font font, ItemStack stack, int xOffset, int yOffset);
}
