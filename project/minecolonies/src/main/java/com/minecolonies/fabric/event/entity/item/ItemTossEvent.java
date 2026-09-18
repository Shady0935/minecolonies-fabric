package com.minecolonies.fabric.event.entity.item;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public class ItemTossEvent extends Event
{
    private final ItemEntity entity;
    private final Player player;
    public ItemTossEvent(final ItemEntity entity, final Player player) { this.entity = entity; this.player = player; }
    public ItemEntity getEntity() { return entity; }
    public Player getPlayer() { return player; }
}
