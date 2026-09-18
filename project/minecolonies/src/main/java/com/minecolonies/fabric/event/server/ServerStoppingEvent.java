package com.minecolonies.fabric.event.server;

import com.minecolonies.fabric.event.Event;
import net.minecraft.server.MinecraftServer;

public class ServerStoppingEvent extends Event
{
    private final MinecraftServer server;
    public ServerStoppingEvent(final MinecraftServer server) { this.server = server; }
    public MinecraftServer getServer() { return server; }
}
