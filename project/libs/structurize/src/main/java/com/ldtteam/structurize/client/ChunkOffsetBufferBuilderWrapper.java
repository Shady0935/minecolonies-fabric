package com.ldtteam.structurize.client;

import com.mojang.blaze3d.vertex.BufferBuilder;

/**
 * Compatibility entry point retained for callers of the old Forge renderer.
 * Fabric's vanilla buffer path already receives transformed vertices, so no
 * delegating BufferBuilder subclass is needed here.
 */
public final class ChunkOffsetBufferBuilderWrapper
{
    private ChunkOffsetBufferBuilderWrapper()
    {
    }

    public static BufferBuilder setupGlobalInstance(final BufferBuilder delegate,
        final int offsetX,
        final int offsetY,
        final int offsetZ)
    {
        return delegate;
    }
}
