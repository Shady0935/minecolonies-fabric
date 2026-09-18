package com.minecolonies.coremod.colony.interactionhandling.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.fabric.registry.FabricRegistry;

public interface IInteractionResponseHandlerRegistry
{
    static FabricRegistry<InteractionResponseHandlerEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getInteractionResponseHandlerRegistry();
    }
}
