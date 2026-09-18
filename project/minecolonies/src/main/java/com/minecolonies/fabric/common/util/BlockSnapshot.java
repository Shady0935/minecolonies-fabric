package com.minecolonies.fabric.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public final class BlockSnapshot
{
    private final ResourceKey<Level> dimension;
    private final LevelAccessor level;
    private final BlockPos pos;

    private BlockSnapshot(final ResourceKey<Level> dimension, final LevelAccessor level, final BlockPos pos)
    {
        this.dimension = dimension;
        this.level = level;
        this.pos = pos;
    }

    public static BlockSnapshot create(final ResourceKey<Level> dimension, final LevelAccessor level, final BlockPos pos)
    {
        return new BlockSnapshot(dimension, level, pos);
    }

    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }

    public LevelAccessor getLevel()
    {
        return level;
    }

    public BlockPos getPos()
    {
        return pos;
    }
}
