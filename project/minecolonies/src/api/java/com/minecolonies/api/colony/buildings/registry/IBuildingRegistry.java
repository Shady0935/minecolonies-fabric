package com.minecolonies.api.colony.buildings.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.fabric.registry.FabricRegistry;

public interface IBuildingRegistry
{

    static FabricRegistry<BuildingEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getBuildingRegistry();
    }
}
