package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public class EntityItemPickupEvent extends Event
{
    private final Player entity;
    private final ItemEntity item;
    public EntityItemPickupEvent(final Player entity, final ItemEntity item) { this.entity = entity; this.item = item; }
    public Player getEntity() { return entity; }
    public ItemEntity getItem() { return item; }
}
