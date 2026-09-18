package com.ldtteam.structurize.config;

/** Root configuration.  Fabric keeps the same access API and defaults. */
public final class Configuration
{
    private final ServerConfiguration server = new ServerConfiguration();

    public ServerConfiguration getServer()
    {
        return server;
    }
}
