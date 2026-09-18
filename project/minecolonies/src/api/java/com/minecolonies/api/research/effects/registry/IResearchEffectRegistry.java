package com.minecolonies.api.research.effects.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.fabric.registry.FabricRegistry;

public interface IResearchEffectRegistry
{
    static FabricRegistry<ResearchEffectEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getResearchEffectRegistry();
    }
}
