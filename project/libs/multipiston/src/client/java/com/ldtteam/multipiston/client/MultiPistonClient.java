package com.ldtteam.multipiston.client;

import com.ldtteam.multipiston.ModBlocks;
import com.ldtteam.multipiston.TileEntityMultiPiston;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;

/** Client-only hooks for opening the upstream BlockUI configuration window. */
public final class MultiPistonClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide || !level.getBlockState(hitResult.getBlockPos()).is(ModBlocks.MULTIPISTON))
            {
                return InteractionResult.PASS;
            }
            if (level.getBlockEntity(hitResult.getBlockPos()) instanceof TileEntityMultiPiston)
            {
                new WindowMultiPiston(hitResult.getBlockPos()).open();
            }
            return InteractionResult.PASS;
        });
    }
}
