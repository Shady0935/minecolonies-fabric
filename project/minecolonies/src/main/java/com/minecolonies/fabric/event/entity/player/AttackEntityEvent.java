package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class AttackEntityEvent extends Event
{
    private final Player entity;
    private final Entity target;
    public AttackEntityEvent(final Player entity, final Entity target) { this.entity = entity; this.target = target; }
    public Player getEntity() { return entity; }
    public Entity getTarget() { return target; }
}
