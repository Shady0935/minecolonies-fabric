package com.ldtteam.multipiston.gametest;

import com.ldtteam.multipiston.ModBlocks;
import com.ldtteam.multipiston.TileEntityMultiPiston;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

/** In-world regression tests for Multi-Piston registration, persistence and movement. */
public final class MultiPistonGameTests implements FabricGameTest
{
    private static final String BATCH = "multipiston_fabric";

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = BATCH, timeoutTicks = 200)
    public void registersBlockItemAndBlockEntity(final GameTestHelper helper)
    {
        helper.assertTrue(BuiltInRegistries.BLOCK.get(new ResourceLocation("multipiston:multipistonblock")) == ModBlocks.MULTIPISTON,
          "Multi-Piston block registry entry is missing");
        helper.assertTrue(BuiltInRegistries.ITEM.get(new ResourceLocation("multipiston:multipistonblock")) == ModBlocks.MULTIPISTON_ITEM,
          "Multi-Piston item registry entry is missing");

        final BlockPos relative = new BlockPos(2, 1, 2);
        helper.setBlock(relative, ModBlocks.MULTIPISTON);
        final BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relative));
        helper.assertTrue(blockEntity instanceof TileEntityMultiPiston,
          "Placing Multi-Piston did not create its block entity");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = BATCH, timeoutTicks = 200)
    public void redstoneStrokeMovesAndRetractsBlock(final GameTestHelper helper)
    {
        final BlockPos relative = new BlockPos(2, 2, 2);
        final BlockPos pistonPos = helper.absolutePos(relative);
        helper.setBlock(relative, ModBlocks.MULTIPISTON);
        final ServerLevel level = helper.getLevel();
        final BlockEntity rawBlockEntity = level.getBlockEntity(pistonPos);
        helper.assertTrue(rawBlockEntity instanceof TileEntityMultiPiston,
          "Multi-Piston block entity was not created for movement test");
        final TileEntityMultiPiston multiPiston = (TileEntityMultiPiston) rawBlockEntity;
        multiPiston.setConfiguration(Direction.UP, Direction.DOWN, 1, 3);

        helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
        multiPiston.handleRedstone(true);
        multiPiston.tick();
        helper.assertTrue(level.getBlockState(pistonPos.above()).is(Blocks.STONE),
          "Powered Multi-Piston did not move the source block to its output face");
        helper.assertTrue(level.isEmptyBlock(pistonPos.below()),
          "Powered Multi-Piston left the moved block at its source");

        multiPiston.handleRedstone(false);
        multiPiston.tick();
        helper.assertTrue(level.getBlockState(pistonPos.below()).is(Blocks.STONE),
          "Unpowered Multi-Piston did not retract the block to its input face");
        helper.assertTrue(level.isEmptyBlock(pistonPos.above()),
          "Unpowered Multi-Piston left the block at its output face");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = BATCH, timeoutTicks = 200)
    public void configurationAndActiveStrokeSurviveNbtReload(final GameTestHelper helper)
    {
        final BlockPos relative = new BlockPos(2, 1, 2);
        helper.setBlock(relative, ModBlocks.MULTIPISTON);
        final BlockPos pos = helper.absolutePos(relative);
        final TileEntityMultiPiston source = (TileEntityMultiPiston) helper.getLevel().getBlockEntity(pos);
        source.setConfiguration(Direction.NORTH, Direction.EAST, 8, 3);
        source.handleRedstone(true);

        final CompoundTag saved = source.saveWithId();
        final TileEntityMultiPiston restored = new TileEntityMultiPiston(pos, helper.getLevel().getBlockState(pos));
        restored.load(saved);
        helper.assertTrue(restored.getInput() == Direction.NORTH && restored.getOutput() == Direction.EAST,
          "Configured faces did not survive NBT reload");
        helper.assertTrue(restored.getRange() == 8 && restored.getSpeed() == 3,
          "Configured range/speed did not survive NBT reload");
        helper.assertTrue(restored.isOn() && restored.getProgress() == 0
              && restored.getCurrentDirection() == Direction.EAST,
          "The active redstone stroke did not survive NBT reload");
        helper.succeed();
    }
}
