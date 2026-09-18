package com.minecolonies.api.colony.guardtype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.fabric.registry.FabricRegistry;

public interface IGuardTypeRegistry
{

    static FabricRegistry<GuardType> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getGuardTypeRegistry();
    }
}
