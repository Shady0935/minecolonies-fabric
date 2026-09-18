package com.minecolonies.fabric.event.server;

import com.minecolonies.fabric.event.Event;
import net.minecraft.server.MinecraftServer;

public class ServerAboutToStartEvent extends Event
{
    private final MinecraftServer server;
    public ServerAboutToStartEvent(final MinecraftServer server) { this.server = server; }
    public MinecraftServer getServer() { return server; }
}
