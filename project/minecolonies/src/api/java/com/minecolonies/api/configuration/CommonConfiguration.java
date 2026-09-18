package com.minecolonies.api.configuration;

import com.minecolonies.fabric.config.FabricConfigSpec;

public class CommonConfiguration extends AbstractConfiguration
{
    public final FabricConfigSpec.BooleanValue generateSupplyLoot;
    public final FabricConfigSpec.BooleanValue rsEnableDebugLogging;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected CommonConfiguration(final FabricConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");
        generateSupplyLoot = defineBoolean(builder, "generatesupplyloot", true);
        finishCategory(builder);

        createCategory(builder, "requestsystem");
        rsEnableDebugLogging = defineBoolean(builder, "enabledebuglogging", false);
        finishCategory(builder);
    }
}
