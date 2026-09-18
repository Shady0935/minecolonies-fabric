package net.minecraftforge.server;

import net.minecraft.server.MinecraftServer;

/** Server lifecycle holder populated by the Fabric entrypoint callbacks. */
public final class ServerLifecycleHooks
{
    private static volatile MinecraftServer currentServer;

    private ServerLifecycleHooks()
    {
    }

    public static MinecraftServer getCurrentServer()
    {
        return currentServer;
    }

    public static void setCurrentServer(final MinecraftServer server)
    {
        currentServer = server;
    }
}
