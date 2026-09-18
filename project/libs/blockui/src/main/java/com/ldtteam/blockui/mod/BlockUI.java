package com.ldtteam.blockui.mod;

import net.fabricmc.api.ModInitializer;

public class BlockUI implements ModInitializer
{
    public static final String MOD_ID = "blockui";

    @Override
    public void onInitialize()
    {
        Log.getLogger().debug("Initialized common BlockUI services for Fabric");
    }
}
