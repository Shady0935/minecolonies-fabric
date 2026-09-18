package com.minecolonies.fabric.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

public class TickEvent extends Event
{
    public enum Phase { START, END }

    public static class ServerTickEvent extends TickEvent
    {
        public final Phase phase;
        public ServerTickEvent(final Phase phase) { this.phase = phase; }
        public MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }
    }

    public static class ClientTickEvent extends TickEvent
    {
        public final Phase phase;
        public ClientTickEvent(final Phase phase) { this.phase = phase; }
    }

    public static class LevelTickEvent extends TickEvent
    {
        public final Level level;
        public final Phase phase;
        public LevelTickEvent(final Level level, final Phase phase) { this.level = level; this.phase = phase; }
    }

    public static class PlayerTickEvent extends TickEvent
    {
        public final Player player;
        public final Phase phase;
        public PlayerTickEvent(final Player player, final Phase phase) { this.player = player; this.phase = phase; }
    }
}
