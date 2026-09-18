package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.fabric.event.Event;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PlayerEvent extends Event
{
    private final Player entity;
    protected PlayerEvent(final Player entity) { this.entity = entity; }
    public Player getEntity() { return entity; }

    public static class PlayerLoggedInEvent extends PlayerEvent { public PlayerLoggedInEvent(final Player player) { super(player); } }
    public static class PlayerLoggedOutEvent extends PlayerEvent { public PlayerLoggedOutEvent(final Player player) { super(player); } }
    public static class PlayerChangedDimensionEvent extends PlayerEvent
    {
        private final ResourceKey<Level> from;
        private final ResourceKey<Level> to;
        public PlayerChangedDimensionEvent(final Player player, final ResourceKey<Level> from, final ResourceKey<Level> to) { super(player); this.from = from; this.to = to; }
        public ResourceKey<Level> getFrom() { return from; }
        public ResourceKey<Level> getTo() { return to; }
    }
}
