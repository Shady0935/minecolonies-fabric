package com.minecolonies.coremod.event.capabilityproviders;

import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.util.ChunkLoadStorage;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import com.minecolonies.fabric.capability.Capability;
import com.minecolonies.fabric.capability.ICapabilitySerializable;
import com.minecolonies.fabric.util.LazyOptional;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.MineColonies.CHUNK_STORAGE_UPDATE_CAP;

/**
 * Capability provider for the world capability of Minecolonies.
 */
public class MinecoloniesWorldCapabilityProvider implements ICapabilitySerializable<Tag>
{
    /**
     * The chunk map capability.
     */
    private final IChunkmanagerCapability chunkMap;

    /**
     * The chunk map capability optional.
     */
    private final LazyOptional<IChunkmanagerCapability> chunkMapOptional;

    /**
     * Constructor of the provider.
     */
    public MinecoloniesWorldCapabilityProvider()
    {
        this(() -> { });
    }

    public MinecoloniesWorldCapabilityProvider(final Runnable markDirty)
    {
        this.chunkMap = new IChunkmanagerCapability.Impl()
        {
            @Override
            public boolean addChunkStorage(final int x, final int z, final ChunkLoadStorage storage)
            {
                final boolean merged = super.addChunkStorage(x, z, storage);
                markDirty.run();
                return merged;
            }

            @Override
            public ChunkLoadStorage getChunkStorage(final int x, final int z)
            {
                final ChunkLoadStorage storage = super.getChunkStorage(x, z);
                if (storage != null)
                {
                    markDirty.run();
                }
                return storage;
            }
        };
        this.chunkMapOptional = LazyOptional.of(() -> chunkMap);
    }

    @Override
    public Tag serializeNBT()
    {
        return IChunkmanagerCapability.Storage.writeNBT(CHUNK_STORAGE_UPDATE_CAP, chunkMap, null);
    }

    @Override
    public void deserializeNBT(final Tag nbt)
    {
        IChunkmanagerCapability.Storage.readNBT(CHUNK_STORAGE_UPDATE_CAP, chunkMap, null, nbt);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> cap, final Direction direction)
    {
        return cap == CHUNK_STORAGE_UPDATE_CAP ? chunkMapOptional.cast() : LazyOptional.empty();
    }
}
