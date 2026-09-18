package com.minecolonies.fabric.common;

import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public interface IPlantable
{
    default boolean canSustainPlant(final BlockState state, final BlockGetter world, final BlockPos pos, final Direction facing, final IPlantable plantable)
    {
        return false;
    }
}
