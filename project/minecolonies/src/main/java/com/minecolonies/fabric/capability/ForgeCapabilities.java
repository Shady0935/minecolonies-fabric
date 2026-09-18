package com.minecolonies.fabric.capability;

import com.minecolonies.fabric.inventory.IItemHandler;

/** Capability identities used by integrations that still expose Forge-style handlers. */
public final class ForgeCapabilities
{
    public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>();

    private ForgeCapabilities()
    {
    }
}
