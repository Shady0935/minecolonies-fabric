package com.minecolonies.fabric.capability;

import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persists claim/unclaim operations waiting for unloaded chunks, separately for each dimension. */
public final class MinecoloniesChunkUpdateSavedData extends SavedData
{
    private final MinecoloniesWorldCapabilityProvider provider = new MinecoloniesWorldCapabilityProvider(this::setDirty);

    private MinecoloniesChunkUpdateSavedData()
    {
    }

    private MinecoloniesChunkUpdateSavedData(final CompoundTag tag)
    {
        provider.deserializeNBT(tag);
    }

    public static MinecoloniesChunkUpdateSavedData get(final ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(
          MinecoloniesChunkUpdateSavedData::new, MinecoloniesChunkUpdateSavedData::new, "minecolonies_chunk_updates");
    }

    public MinecoloniesWorldCapabilityProvider getProvider()
    {
        return provider;
    }

    @Override
    public CompoundTag save(final CompoundTag tag)
    {
        tag.merge((CompoundTag) provider.serializeNBT());
        return tag;
    }
}
