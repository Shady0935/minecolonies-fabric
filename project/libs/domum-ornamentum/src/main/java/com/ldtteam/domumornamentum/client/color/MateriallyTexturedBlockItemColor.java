package com.ldtteam.domumornamentum.client.color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class MateriallyTexturedBlockItemColor implements ItemColor
{
    private static final int TINT_MASK = 0xff;
    private static final int TINT_BITS = 8;

    @Override
    public int getColor(
            @NotNull final ItemStack stack,
            final int tint )
    {
        final BlockState state = Block.stateById (tint >> TINT_BITS);
        if(state.getBlock() instanceof LiquidBlock) {
            return FluidRenderHandlerRegistry.INSTANCE.get(state.getFluidState().getType())
                .getFluidColor(null, BlockPos.ZERO, state.getFluidState());
        }

        final ItemStack workingStack = new ItemStack(state.getBlock(), 1);
        if (workingStack.getItem() instanceof AirItem)
            return 0xffffff;

        final Block block = state.getBlock();
        final Item itemFromBlock = block.asItem();
        int tintValue = tint & TINT_MASK;
        return Minecraft.getInstance().getBlockColors().getColor(state, null, null, tintValue);
    }
}
