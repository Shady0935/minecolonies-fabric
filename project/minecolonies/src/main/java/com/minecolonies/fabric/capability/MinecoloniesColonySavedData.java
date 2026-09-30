package com.minecolonies.fabric.capability;

import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldColonyManagerCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persists the per-dimension colony capability through Minecraft's normal world-save pipeline. */
public final class MinecoloniesColonySavedData extends SavedData
{
    private static final String DATA_ID = "minecolonies_colonies";

    private CompoundTag storedState;
    private MinecoloniesWorldColonyManagerCapabilityProvider provider;

    private MinecoloniesColonySavedData()
    {
        storedState = null;
    }

    private MinecoloniesColonySavedData(final CompoundTag storedState)
    {
        this.storedState = storedState.copy();
    }

    public static MinecoloniesColonySavedData get(final ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(
            MinecoloniesColonySavedData::new,
            MinecoloniesColonySavedData::new,
            DATA_ID);
    }

    public MinecoloniesWorldColonyManagerCapabilityProvider getProvider()
    {
        if (provider == null)
        {
            provider = new MinecoloniesWorldColonyManagerCapabilityProvider();
            if (storedState != null)
            {
                provider.deserializeNBT(storedState.copy());
                storedState = null;
            }
        }
        return provider;
    }

    @Override
    public CompoundTag save(final CompoundTag tag)
    {
        if (provider != null)
        {
            tag.merge((CompoundTag) provider.serializeNBT());
        }
        else if (storedState != null)
        {
            tag.merge(storedState);
        }
        return tag;
    }

    /** Keeps the snapshot eligible for Minecraft's periodic and shutdown saves. */
    public static void markDirty(final ServerLevel level)
    {
        get(level).setDirty();
    }
}
