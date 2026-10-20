package com.minecolonies.api.configuration;

import com.ldtteam.structurize.config.fabric.FabricConfigSpec;

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
    private final FabricConfigSpec clientSpec;
    private final FabricConfigSpec serverSpec;
    private final FabricConfigSpec commonSpec;

    /**
     * Builds configuration tree.
     */
    public Configuration()
    {
        final FabricConfigSpec.Builder clientBuilder = new FabricConfigSpec.Builder();
        final FabricConfigSpec.Builder serverBuilder = new FabricConfigSpec.Builder();
        final FabricConfigSpec.Builder commonBuilder = new FabricConfigSpec.Builder();
        clientConfig = new ClientConfiguration(clientBuilder);
        serverConfig = new ServerConfiguration(serverBuilder);
        commonConfiguration = new CommonConfiguration(commonBuilder);
        clientSpec = clientBuilder.build();
        serverSpec = serverBuilder.build();
        commonSpec = commonBuilder.build();
    }

    public FabricConfigSpec clientSpec() { return clientSpec; }
    public FabricConfigSpec serverSpec() { return serverSpec; }
    public FabricConfigSpec commonSpec() { return commonSpec; }

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
