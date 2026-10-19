package com.minecolonies.fabric.gametest;

import com.ldtteam.domumornamentum.block.decorative.TimberFrameBlock;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.AbstractPathJob;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/** Regression probes for path origins above lower, independently walkable floors. */
public final class MineColoniesPathStartGameTests implements FabricGameTest
{
    @GameTestGenerator
    public Collection<TestFunction> pathStartTests()
    {
        return List.of(
          test("domum_floor", helper -> upperFloor(helper, BuiltInRegistries.BLOCK.stream()
            .filter(TimberFrameBlock.class::isInstance).findFirst().orElseThrow().defaultBlockState(), 6.0D)),
          test("glass_floor", helper -> upperFloor(helper, Blocks.GLASS.defaultBlockState(), 6.0D)),
          test("top_slab", helper -> upperFloor(helper, Blocks.OAK_SLAB.defaultBlockState()
            .setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP), 6.0D)),
          test("bottom_slab", helper -> upperFloor(helper, Blocks.OAK_SLAB.defaultBlockState(), 5.5D)),
          test("stairs", helper -> upperFloor(helper, Blocks.OAK_STAIRS.defaultBlockState(), 6.0D)),
          test("closed_trapdoor", helper -> upperFloor(helper, Blocks.OAK_TRAPDOOR.defaultBlockState()
            .setValue(BlockStateProperties.HALF, Half.TOP), 6.0D)),
          test("carpet", helper -> upperFloor(helper, Blocks.WHITE_CARPET.defaultBlockState(), 5.0625D)),
          test("platform_edge", MineColoniesPathStartGameTests::platformEdge),
          test("grounded_door", MineColoniesPathStartGameTests::groundedDoor),
          test("unobstructed_fall", MineColoniesPathStartGameTests::unobstructedFall));
    }

    private static TestFunction test(final String name, final Consumer<GameTestHelper> probe)
    {
        return new TestFunction("minecolonies_path_start", "path_start_" + name,
          FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, probe);
    }

    private static void clearColumn(final GameTestHelper helper)
    {
        for (int x = 0; x <= 2; x++)
        {
            for (int z = 0; z <= 2; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                for (int y = 1; y <= 8; y++)
                {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static Villager mobAt(final GameTestHelper helper, final double x, final double y, final double z)
    {
        final Villager mob = EntityType.VILLAGER.create(helper.getLevel());
        helper.assertTrue(mob != null, "Could not create path-origin probe mob");
        final BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        mob.setPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
        return mob;
    }

    private static void upperFloor(final GameTestHelper helper, final BlockState floor, final double feetY)
    {
        clearColumn(helper);
        helper.setBlock(new BlockPos(1, 5, 1), floor);
        final Villager mob = mobAt(helper, 1.5D, feetY, 1.5D);
        mob.setOnGround(true);
        // A half slab uses the integer node above its fractional top surface.
        final BlockPos expected = floor.is(Blocks.OAK_SLAB)
                                    && floor.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM
                                    ? mob.blockPosition().above() : mob.blockPosition();
        assertStart(helper, mob, expected);
    }

    private static void platformEdge(final GameTestHelper helper)
    {
        clearColumn(helper);
        helper.setBlock(new BlockPos(0, 5, 1), Blocks.STONE);
        // Its center is over air, but part of its feet remains supported by the platform.
        final Villager mob = mobAt(helper, 1.1D, 6.0D, 1.5D);
        mob.setOnGround(true);
        assertStart(helper, mob, mob.blockPosition());
    }

    private static void groundedDoor(final GameTestHelper helper)
    {
        clearColumn(helper);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.OAK_DOOR);
        final Villager mob = mobAt(helper, 1.5D, 1.0D, 1.5D);
        mob.setOnGround(true);
        assertStart(helper, mob, mob.blockPosition());
    }

    private static void unobstructedFall(final GameTestHelper helper)
    {
        clearColumn(helper);
        final Villager mob = mobAt(helper, 1.5D, 4.0D, 1.5D);
        assertStart(helper, mob, helper.absolutePos(new BlockPos(1, 1, 1)));
    }

    private static void assertStart(final GameTestHelper helper, final Villager mob, final BlockPos expected)
    {
        final BlockPos actual = AbstractPathJob.prepareStart(mob);
        helper.assertTrue(expected.equals(actual),
          "Path origin crossed a support: entity=" + mob.position() + "; expected=" + expected + "; actual=" + actual);
        helper.succeed();
    }
}
