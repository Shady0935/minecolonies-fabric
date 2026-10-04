package com.minecolonies.fabric.gametest;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.storage.BlueprintPlacementHandling;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.minecolonies.fabric.compat.FabricVanillaCompat;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Opt-in creative placement probes, also runnable from a packaged intermediary server. */
public final class MineColoniesCreativePlacementGameTests implements FabricGameTest
{
    @GameTestGenerator
    public Collection<TestFunction> creativePlacementTests()
    {
        if (!Boolean.getBoolean("minecolonies.creative-placement-tests"))
        {
            return List.of();
        }
        return List.of(test("creative_miner_pretty", BuildToolPlacementMessage.HandlerType.Pretty),
          test("creative_miner_complete", BuildToolPlacementMessage.HandlerType.Complete));
    }

    private static TestFunction test(final String name, final BuildToolPlacementMessage.HandlerType type)
    {
        return new TestFunction("minecolonies_creative", name, FabricGameTest.EMPTY_STRUCTURE, 1200, 0, true,
          helper -> placeMiner(helper, name, type));
    }

    private static void placeMiner(final GameTestHelper helper, final String name,
      final BuildToolPlacementMessage.HandlerType type)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Pack loading interrupted");
        final var level = helper.getLevel();
        final BlockPos center = helper.absolutePos(new BlockPos(8, 1, 8));
        final BlockPos minerPos = center.offset(32, 0, 32);
        final ChunkPos centerChunk = new ChunkPos(center);
        for (int x = centerChunk.x - 3; x <= centerChunk.x + 5; x++)
        {
            for (int z = centerChunk.z - 3; z <= centerChunk.z + 5; z++)
            {
                level.setChunkForced(x, z, true);
                level.getChunk(x, z);
            }
        }
        final ServerPlayer owner = new ServerPlayer(level.getServer(), level,
          new GameProfile(UUID.randomUUID(), "CreativeMinerProbe"));
        level.getServer().getPlayerList().placeNewPlayer(new Connection(PacketFlow.SERVERBOUND), owner);
        owner.setGameMode(GameType.CREATIVE);
        owner.teleportTo(center.getX(), center.getY() + 3, center.getZ());
        level.setBlockAndUpdate(center, ModBlocks.blockHutTownHall.defaultBlockState());
        final IColony colony = IColonyManager.getInstance().createColony(
          level, center, owner, name, Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Creative placement colony was not created");
        final var townHall = (TileEntityColonyBuilding) level.getBlockEntity(center);
        townHall.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHall.setBlueprintPath("fundamentals/townhall1.blueprint");
        colony.getBuildingManager().addNewBuilding(townHall, level);
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, center, 5, level, true);

        helper.runAfterDelay(10, () ->
        {
            helper.assertTrue(WorldUtil.isBlockLoaded(level, minerPos), "Loaded miner chunk reported unloaded");
            final long key = new ChunkPos(minerPos).toLong();
            helper.assertTrue(FabricVanillaCompat.getVisibleChunk(level.getChunkSource(), key) != null,
              "Visible chunk lookup failed in " + FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace());
            helper.assertTrue(FabricVanillaCompat.getVisibleChunkKeys(level.getChunkSource()).contains(key),
              "Visible chunk keys omitted the loaded miner chunk");
            final var message = new BuildToolPlacementMessage(type, "", "Pagoda", "fundamentals/miner1.blueprint",
              minerPos, Rotation.CLOCKWISE_90, Mirror.FRONT_BACK);
            message.world = level;
            message.player = owner;
            BlueprintPlacementHandling.handlePlacement(message);
        });

        helper.succeedWhen(() ->
        {
            helper.assertTrue(!Manager.getChangeStoragesForPlayer(owner.getUUID()).isEmpty(),
              "Creative placement has not completed its ticked world operation");
            helper.assertTrue(level.getBlockState(minerPos).getBlock() == ModBlocks.blockHutMiner,
              "Creative placement lost the miner hut anchor");
            if (type == BuildToolPlacementMessage.HandlerType.Complete)
            {
                // Schematic Paste preserves the raw blueprint. Upstream does
                // not activate/register it as a constructed colony building.
                helper.assertTrue(level.getBlockEntity(minerPos) instanceof TileEntityColonyBuilding,
                  "Raw schematic placement lost the miner block entity");
                final var hut = (TileEntityColonyBuilding) level.getBlockEntity(minerPos);
                helper.assertTrue("miner1".equals(hut.getSchematicName()), "Raw mine lost its schematic data");
                final var reloaded = new TileEntityColonyBuilding(minerPos, level.getBlockState(minerPos));
                reloaded.load(hut.saveWithFullMetadata());
                helper.assertTrue("miner1".equals(reloaded.getSchematicName()), "Raw mine failed block entity NBT reload");
                logSuccess(name);
                return;
            }
            final var building = colony.getBuildingManager().getBuilding(minerPos);
            helper.assertTrue(building instanceof BuildingMiner, "Creative placement did not register a mine");
            helper.assertTrue(building.getTileEntity() == level.getBlockEntity(minerPos), "Mine lost its block entity");
            helper.assertTrue("Pagoda".equals(building.getStructurePack()), "Mine lost its selected pack");
            helper.assertTrue("fundamentals/miner1.blueprint".equals(building.getBlueprintPath()), "Mine lost its path");
            helper.assertTrue(building.getBuildingLevel() == 1 && building.isBuilt(), "Mine is not built at level one");
            helper.assertTrue(building.isMirrored(), "Mine lost its mirror setting");
            final Colony restored = Colony.loadColony(colony.getColonyTag().copy(), level);
            final var restoredMine = restored.getBuildingManager().getBuilding(minerPos);
            helper.assertTrue(restoredMine instanceof BuildingMiner && restoredMine.getBuildingLevel() == 1,
              "Mine did not survive colony NBT reload");
            helper.assertTrue("fundamentals/miner1.blueprint".equals(restoredMine.getBlueprintPath()),
              "Mine path did not survive colony NBT reload");
            logSuccess(name);
        });
    }

    private static void logSuccess(final String name)
    {
        LoggerFactory.getLogger(MineColoniesCreativePlacementGameTests.class).info(
          "Creative placement {} passed all assertions in {}", name,
          FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace());
    }
}
