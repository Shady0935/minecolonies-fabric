package com.minecolonies.api.configuration;

import com.minecolonies.fabric.config.FabricConfigSpec;

/**
 * Mod client configuration. Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    public final FabricConfigSpec.BooleanValue citizenVoices;
    public final FabricConfigSpec.BooleanValue neighborbuildingrendering;
    public final FabricConfigSpec.IntValue neighborbuildingrange;
    public final FabricConfigSpec.IntValue buildgogglerange;
    public final FabricConfigSpec.BooleanValue colonyteamborders;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final FabricConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");
        citizenVoices = defineBoolean(builder, "enablecitizenvoices", true);
        neighborbuildingrendering = defineBoolean(builder, "neighborbuildingrendering", true);
        neighborbuildingrange = defineInteger(builder, "neighborbuildingrange", 4, -2, 16);
        buildgogglerange = defineInteger(builder, "buildgogglerange", 50, 1, 250);
        colonyteamborders = defineBoolean(builder, "colonyteamborders", true);

        swapToCategory(builder, "pathfinding");

        finishCategory(builder);
    }
}
