package com.minecolonies.fabric.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class OnDatapackSyncEvent extends Event
{
    private final MinecraftServer server;
    private final ServerPlayer player;

    public OnDatapackSyncEvent(final MinecraftServer server, final ServerPlayer player)
    {
        this.server = server;
        this.player = player;
    }

    public MinecraftServer getServer() { return server; }
    public ServerPlayer getPlayer() { return player; }
    public PlayerList getPlayerList() { return server.getPlayerList(); }
}
