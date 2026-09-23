package com.ldtteam.multipiston;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A configurable redstone powered block mover. */
public final class MultiPistonBlock extends BaseEntityBlock
{
    private static final float BLOCK_HARDNESS = 1.0F;
    private static final float RESISTANCE = 1.0F;
    private static final VoxelShape OUTLINE = Block.box(0.01D, 0.01D, 0.01D, 15.99D, 15.99D, 15.99D);

    public MultiPistonBlock()
    {
        super(Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE)
          .strength(BLOCK_HARDNESS, RESISTANCE).isRedstoneConductor((state, level, pos) -> true));
    }

    @Override
    public InteractionResult use(final BlockState state, final Level level, final BlockPos pos,
                                 final Player player, final InteractionHand hand, final BlockHitResult hit)
    {
        // The client entrypoint opens the BlockUI window. Returning success on
        // both sides prevents a held item from being used through this block.
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter level,
                                       final BlockPos pos, final CollisionContext context)
    {
        return Shapes.block();
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter level,
                               final BlockPos pos, final CollisionContext context)
    {
        return OUTLINE;
    }

    @Override
    public void neighborChanged(final BlockState state, final Level level, final BlockPos pos,
                                final Block neighborBlock, final BlockPos neighborPos,
                                final boolean movedByPiston)
    {
        if (level.isClientSide)
        {
            return;
        }
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityMultiPiston multiPiston)
        {
            multiPiston.handleRedstone(level.hasNeighborSignal(pos));
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new TileEntityMultiPiston(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                  final BlockEntityType<T> type)
    {
        return createTickerHelper(type, ModTileEntities.MULTIPISTON,
          (world, pos, blockState, blockEntity) -> blockEntity.tick());
    }

    @Override
    public RenderShape getRenderShape(final BlockState state)
    {
        return RenderShape.MODEL;
    }
}
