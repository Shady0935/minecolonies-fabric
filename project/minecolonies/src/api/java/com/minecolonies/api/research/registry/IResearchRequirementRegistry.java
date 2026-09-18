package com.minecolonies.api.research.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.fabric.registry.FabricRegistry;

public interface IResearchRequirementRegistry
{

    static FabricRegistry<ResearchRequirementEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getResearchRequirementRegistry();
    }
}
