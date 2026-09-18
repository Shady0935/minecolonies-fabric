package com.minecolonies.api.colony.jobs.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.fabric.registry.FabricRegistry;

public interface IJobRegistry
{
    static FabricRegistry<JobEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getJobRegistry();
    }
}
