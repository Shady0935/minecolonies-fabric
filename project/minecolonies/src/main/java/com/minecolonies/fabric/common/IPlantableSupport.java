package com.minecolonies.fabric.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class IPlantableSupport
{
    private IPlantableSupport()
    {
    }

    public static boolean canSustainPlant(final IPlantable block, final BlockState state, final BlockGetter world, final BlockPos pos, final Direction facing)
    {
        return block.canSustainPlant(state, world, pos, facing, block);
    }
}
