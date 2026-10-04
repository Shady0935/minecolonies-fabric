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
    private final boolean loadedFromDisk;
    private boolean claimsMigrated;
    private RuntimeException loadFailure;
    private boolean initializing;

    private MinecoloniesColonySavedData()
    {
        storedState = null;
        loadedFromDisk = false;
    }

    private MinecoloniesColonySavedData(final CompoundTag storedState)
    {
        this.storedState = storedState.copy();
        loadedFromDisk = true;
        claimsMigrated = storedState.getInt("minecolonies:claims_version") >= 1;
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
        }
        return provider;
    }

    /** Bind the provider to the level BEFORE loading: colony loading can look it up recursively. */
    public void initializeProvider()
    {
        if (storedState != null && !initializing)
        {
            initializing = true;
            try
            {
                getProvider().deserializeNBT(storedState);
                storedState = null;
            }
            catch (final RuntimeException exception)
            {
                loadFailure = exception;
                throw exception;
            }
            finally
            {
                initializing = false;
            }
        }
    }

    public boolean wasLoadedFromDisk()
    {
        return loadedFromDisk;
    }

    public boolean needsClaimMigration()
    {
        return !claimsMigrated;
    }

    public void finishClaimMigration()
    {
        claimsMigrated = true;
        setDirty();
    }

    @Override
    public CompoundTag save(final CompoundTag tag)
    {
        if (loadFailure != null)
        {
            throw new IllegalStateException("Refusing to overwrite colony data after a failed native load", loadFailure);
        }
        if (storedState != null)
        {
            tag.merge(storedState);
        }
        else if (provider != null)
        {
            tag.merge((CompoundTag) provider.serializeNBT());
        }
        tag.putInt("minecolonies:claims_version", claimsMigrated ? 1 : 0);
        return tag;
    }

    /** Keeps the snapshot eligible for Minecraft's periodic and shutdown saves. */
    public static void markDirty(final ServerLevel level)
    {
        get(level).setDirty();
    }
}
