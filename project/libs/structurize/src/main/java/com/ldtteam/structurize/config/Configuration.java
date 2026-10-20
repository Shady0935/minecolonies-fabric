package com.ldtteam.structurize.config;

/** Root configuration.  Fabric keeps the same access API and defaults. */
public final class Configuration
{
    private final ServerConfiguration server;
    private final com.ldtteam.structurize.config.fabric.FabricConfigSpec spec;

    public Configuration()
    {
        final var builder = new com.ldtteam.structurize.config.fabric.FabricConfigSpec.Builder();
        server = new ServerConfiguration(builder);
        spec = builder.build();
    }

    public com.ldtteam.structurize.config.fabric.FabricConfigSpec serverSpec() { return spec; }

    public ServerConfiguration getServer()
    {
        return server;
    }
}
