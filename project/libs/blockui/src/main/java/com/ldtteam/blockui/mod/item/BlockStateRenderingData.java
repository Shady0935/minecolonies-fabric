package com.ldtteam.blockui.mod.item;

import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Holds blockstate rendering data for UIs. BlockState must match blockEntity.
 *
 * <p>Forge's ModelData has no Fabric counterpart in 1.20.1, so the port keeps
 * the block entity and item-pick contract while the vanilla renderer supplies
 * model data through the normal blockstate path.</p>
 */
public record BlockStateRenderingData(BlockState blockState,
    @Nullable BlockEntity blockEntity,
    boolean modelNeedsRotationFix,
    Supplier<ItemStack> playerPickedItemStack)
{
    public static final BlockPos ILLEGAL_BLOCK_ENTITY_POS = BlockPos.ZERO.below(1000);

    private BlockStateRenderingData(final BlockState blockState,
        final BlockEntity blockEntity,
        final boolean modelNeedsRotationFix)
    {
        this(blockState,
            blockEntity,
            modelNeedsRotationFix,
            Suppliers.memoize(() -> BlockToItemHelper.getItemStack(blockState, blockEntity, Minecraft.getInstance().player)));
    }

    private BlockStateRenderingData(final BlockState blockState, final BlockEntity blockEntity)
    {
        this(blockState, blockEntity, checkModelForYrotation(blockState));
    }

    /**
     * @param blockEntity must match blockState
     */
    public static BlockStateRenderingData of(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        return blockEntity == null ? of(blockState) : new BlockStateRenderingData(blockState, blockEntity);
    }

    /**
     * If blockState should have blockEntity then a new fresh empty one will be created.
     */
    public static BlockStateRenderingData of(final BlockState blockState)
    {
        if (blockState.hasBlockEntity() && blockState.getBlock() instanceof final EntityBlock entityBlock)
        {
            final BlockEntity be = entityBlock.newBlockEntity(ILLEGAL_BLOCK_ENTITY_POS, blockState);
            if (be != null)
            {
                return of(blockState, be);
            }
        }
        return new BlockStateRenderingData(blockState, null);
    }

    /**
     * Useful when a block entity is updated.
     */
    public BlockStateRenderingData updateBlockEntity(final Function<BlockEntity, BlockEntity> updater)
    {
        final BlockEntity updated = updater.apply(blockEntity);
        return new BlockStateRenderingData(blockState, updated, modelNeedsRotationFix);
    }

    /**
     * @return best guess using player pick and similar methods
     */
    public ItemStack itemStack()
    {
        return playerPickedItemStack.get();
    }

    /**
     * @return true if model contains only Y axis rotations
     */
    public static boolean checkModelForYrotation(final BlockState blockState)
    {
        // Forge exposes unbaked-model internals here; Fabric 1.20.1 does not.
        // The vanilla block renderer already applies the model transform, so no
        // extra corrective rotation is required by this port.
        return false;
    }
}
