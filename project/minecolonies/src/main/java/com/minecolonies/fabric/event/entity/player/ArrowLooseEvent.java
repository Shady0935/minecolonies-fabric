package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.player.Player;

public class ArrowLooseEvent extends Event
{
    private final Player entity;
    public ArrowLooseEvent(final Player entity) { this.entity = entity; }
    public Player getEntity() { return entity; }
}
