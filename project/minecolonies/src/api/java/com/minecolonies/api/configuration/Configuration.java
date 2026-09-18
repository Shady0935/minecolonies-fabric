package com.minecolonies.api.configuration;

import com.minecolonies.fabric.config.FabricConfigSpec;

/**
 * Mod root configuration.
 */
public class Configuration
{
    /**
     * Loaded clientside, not synced
     */
    private final ClientConfiguration clientConfig;

    /**
     * Loaded serverside, synced on connection
     */
    private final ServerConfiguration serverConfig;

    /**
     * Loaded serverside, synced on connection
     */
    private final CommonConfiguration commonConfiguration;

    /**
     * Builds configuration tree.
     */
    public Configuration()
    {
        clientConfig = new ClientConfiguration(new FabricConfigSpec.Builder());
        serverConfig = new ServerConfiguration(new FabricConfigSpec.Builder());
        commonConfiguration = new CommonConfiguration(new FabricConfigSpec.Builder());
    }

    public ClientConfiguration getClient()
    {
        return clientConfig;
    }

    public ServerConfiguration getServer()
    {
        return serverConfig;
    }

    public CommonConfiguration getCommon()
    {
        return commonConfiguration;
    }
}
