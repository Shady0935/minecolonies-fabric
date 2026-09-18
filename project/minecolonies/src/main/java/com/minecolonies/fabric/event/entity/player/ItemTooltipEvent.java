package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ItemTooltipEvent extends Event
{
    private final Player entity;
    private final ItemStack itemStack;
    private final List<Component> toolTip;
    public ItemTooltipEvent(final Player entity, final ItemStack itemStack, final List<Component> toolTip) { this.entity = entity; this.itemStack = itemStack; this.toolTip = toolTip; }
    public Player getEntity() { return entity; }
    public ItemStack getItemStack() { return itemStack; }
    public List<Component> getToolTip() { return toolTip; }
}
