package com.minecolonies.fabric.event.level;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ChunkEvent extends Event
{
    private final ChunkAccess chunk;
    private final LevelAccessor level;

    protected ChunkEvent(final ChunkAccess chunk, final LevelAccessor level)
    {
        this.chunk = chunk;
        this.level = level;
    }

    public ChunkAccess getChunk() { return chunk; }
    public LevelAccessor getLevel() { return level; }

    public static class Load extends ChunkEvent { public Load(final ChunkAccess chunk, final LevelAccessor level) { super(chunk, level); } }
    public static class Unload extends ChunkEvent { public Unload(final ChunkAccess chunk, final LevelAccessor level) { super(chunk, level); } }
}
