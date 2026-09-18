package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

public class FillBucketEvent extends Event
{
    private final Player entity;
    private final HitResult target;
    public FillBucketEvent(final Player entity, final HitResult target) { this.entity = entity; this.target = target; }
    public Player getEntity() { return entity; }
    public HitResult getTarget() { return target; }
}
