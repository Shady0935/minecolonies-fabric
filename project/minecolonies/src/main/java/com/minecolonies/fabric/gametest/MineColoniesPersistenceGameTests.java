package com.minecolonies.fabric.gametest;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.util.ChunkLoadStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.minecolonies.fabric.capability.CapabilityHooks;
import com.minecolonies.fabric.capability.MinecoloniesColonySavedData;
import com.minecolonies.fabric.capability.MinecoloniesChunkUpdateSavedData;
import com.ldtteam.structurize.storage.StructurePacks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/** Opt-in, sequential tests. Run each phase in the SAME isolated world, with a real stop/restart between phases. */
public final class MineColoniesPersistenceGameTests implements FabricGameTest
{
    private static final BlockPos CENTER = new BlockPos(40, -60, 40);
    private static final ChunkPos MANUAL = new ChunkPos(220, 220);
    private static final ChunkPos BUILDING = new ChunkPos(221, 220);
    private static final ChunkPos QUEUED = new ChunkPos(240, 240);
    private static final ChunkPos QUEUED_BUILDING = new ChunkPos(241, 240);
    private static final BlockPos BUILDING_ANCHOR = CENTER.offset(10, 0, 0);

    @GameTestGenerator
    public Collection<TestFunction> persistenceTests()
    {
        if (!Boolean.getBoolean("minecolonies.persistence-tests"))
        {
            return List.of();
        }
        return List.of(
          test("claims_prepare", this::prepare),
          test("claims_verify", this::verify),
          test("claims_remove", this::remove),
          test("claims_verify_removed", this::verifyRemoved),
          test("claims_mixed_roundtrip", this::mixedRoundTrip));
    }

    private TestFunction test(final String name, final java.util.function.Consumer<GameTestHelper> action)
    {
        return new TestFunction("minecolonies_persistence", name, FabricGameTest.EMPTY_STRUCTURE, 400, 0, true, helper ->
        {
            action.accept(helper);
            LoggerFactory.getLogger(MineColoniesPersistenceGameTests.class).info("Persistence phase {} passed all assertions", name);
        });
    }

    private static IColonyTagCapability claims(final LevelChunk chunk)
    {
        return CapabilityHooks.getCapability(chunk, IColony.CLOSE_COLONY_CAP, null).resolve().orElseThrow();
    }

    private static IChunkmanagerCapability queue(final ServerLevel level)
    {
        return CapabilityHooks.getCapability(level, MineColonies.CHUNK_STORAGE_UPDATE_CAP, null).resolve().orElseThrow();
    }

    private static LevelChunk chunk(final ServerLevel level, final ChunkPos pos)
    {
        return level.getChunk(pos.x, pos.z);
    }

    private void prepare(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Pack loading interrupted");
        level.setChunkForced(CENTER.getX() >> 4, CENTER.getZ() >> 4, true);
        level.setBlockAndUpdate(CENTER, ModBlocks.blockHutTownHall.defaultBlockState());
        final IColony colony = IColonyManager.getInstance().createColony(
          level, CENTER, helper.makeMockServerPlayerInLevel(), "Claims restart latest", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null && colony.getID() == 1, "Use a fresh isolated probe world");
        final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) level.getBlockEntity(CENTER);
        hut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        hut.setBlueprintPath("fundamentals/townhall1.blueprint");
        colony.getBuildingManager().addNewBuilding(hut, level);
        level.setChunkForced(CENTER.getX() >> 4, CENTER.getZ() >> 4, true);

        final LevelChunk manual = chunk(level, MANUAL);
        claims(manual).addColony(1, manual);
        claims(manual).setOwningColony(1, manual);
        final LevelChunk building = chunk(level, BUILDING);
        claims(building).addBuildingClaim(1, BUILDING_ANCHOR, building);

        // Exercises BOTH serializer injections, independent of the live provider/cache.
        final CompoundTag chunkTag = ChunkSerializer.write(level, manual);
        helper.assertTrue(chunkTag.contains("minecolonies:claims"), "ChunkSerializer did not write claim NBT");
        final LevelChunk reloaded = ((ImposterProtoChunk) ChunkSerializer.read(level, level.getPoiManager(), MANUAL, chunkTag)).getWrapped();
        helper.assertTrue(claims(reloaded).getOwningColony() == 1, "ChunkSerializer did not restore claim NBT");
        helper.assertTrue(claims(reloaded).getStaticClaimColonies().contains(1), "Static claim list lost in chunk reload");

        helper.assertTrue(!level.hasChunk(QUEUED.x, QUEUED.z), "Queued fixture unexpectedly loaded");
        queue(level).addChunkStorage(QUEUED.x, QUEUED.z,
          new ChunkLoadStorage(1, QUEUED.toLong(), true, level.dimension().location(), true));
        queue(level).addChunkStorage(QUEUED_BUILDING.x, QUEUED_BUILDING.z,
          new ChunkLoadStorage(1, QUEUED_BUILDING.toLong(), level.dimension().location(), BUILDING_ANCHOR, true));
        helper.assertTrue(MinecoloniesChunkUpdateSavedData.get(level).isDirty(), "Queue insertion did not mark SavedData dirty");
        final ServerLevel nether = level.getServer().getLevel(Level.NETHER);
        queue(nether).addChunkStorage(QUEUED.x, QUEUED.z,
          new ChunkLoadStorage(9, QUEUED.toLong(), true, nether.dimension().location(), true));
        level.getServer().saveEverything(true, true, true);
        helper.succeed();
    }

    private void verify(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        helper.assertTrue(MinecoloniesColonySavedData.get(level).wasLoadedFromDisk(), "Colony loaded from legacy recovery, not native disk data");
        final IColony colony = IColonyManager.getInstance().getColonyByWorld(1, level);
        helper.assertTrue(colony != null && colony.getName().equals("Claims restart latest"), "Latest colony state lost on restart");
        helper.assertTrue(colony.hasTownHall(), "Town Hall lost on restart");
        helper.assertTrue(claims(chunk(level, MANUAL)).getOwningColony() == 1, "Manual claim lost on restart");
        helper.assertTrue(claims(chunk(level, MANUAL)).getStaticClaimColonies().contains(1), "Static claim lost on restart");
        helper.assertTrue(claims(chunk(level, BUILDING)).getAllClaimingBuildings().getOrDefault(1, java.util.Set.of()).contains(BUILDING_ANCHOR),
          "Building claim lost on restart");

        helper.assertTrue(queue(level).getAllChunkStorages().containsKey(QUEUED), "Unloaded static claim missing from disk");
        helper.assertTrue(queue(level).getAllChunkStorages().containsKey(QUEUED_BUILDING), "Unloaded building claim missing from disk");
        final ServerLevel nether = level.getServer().getLevel(Level.NETHER);
        helper.assertTrue(queue(nether).getAllChunkStorages().containsKey(QUEUED), "Other dimension's queue lost");
        helper.assertTrue(queue(nether).getAllChunkStorages().get(QUEUED).getDimension().equals(Level.NETHER.location()), "Dimension queues mixed");

        // Force the normal chunk-load callback to consume queued updates.
        final LevelChunk queued = chunk(level, QUEUED);
        ChunkDataHelper.loadChunk(queued, level);
        final LevelChunk queuedBuilding = chunk(level, QUEUED_BUILDING);
        ChunkDataHelper.loadChunk(queuedBuilding, level);
        helper.assertTrue(claims(queued).getOwningColony() == 1, "Pending static claim failed to apply");
        helper.assertTrue(claims(queuedBuilding).getAllClaimingBuildings().getOrDefault(1, java.util.Set.of()).contains(BUILDING_ANCHOR),
          "Pending building claim failed to apply (NBT key mismatch)");
        helper.assertTrue(!queue(level).getAllChunkStorages().containsKey(QUEUED), "Applied queue entry not removed");
        helper.assertTrue(MinecoloniesChunkUpdateSavedData.get(level).isDirty(), "Queue consumption did not mark SavedData dirty");
        level.getServer().saveEverything(true, true, true);
        helper.succeed();
    }

    private void remove(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        helper.assertTrue(!queue(level).getAllChunkStorages().containsKey(QUEUED), "Consumed claim replayed on restart");
        helper.assertTrue(claims(chunk(level, QUEUED)).getOwningColony() == 1, "Applied claim not written back to chunk disk NBT");
        helper.assertTrue(claims(chunk(level, QUEUED_BUILDING)).getAllClaimingBuildings().getOrDefault(1, java.util.Set.of()).contains(BUILDING_ANCHOR),
          "Applied building claim not written back to disk");
        final LevelChunk manual = chunk(level, MANUAL);
        claims(manual).removeColony(1, manual);
        final LevelChunk building = chunk(level, BUILDING);
        claims(building).removeBuildingClaim(1, BUILDING_ANCHOR, building);
        // An unloaded removal must survive shutdown as well, without resurrecting a previous claim.
        final ChunkPos removeLater = new ChunkPos(242, 240);
        queue(level).addChunkStorage(removeLater.x, removeLater.z,
          new ChunkLoadStorage(1, removeLater.toLong(), level.dimension().location(), BUILDING_ANCHOR, false));
        IColonyManager.getInstance().getColonyByWorld(1, level).setName("Claims restart newest");
        level.getServer().saveEverything(true, true, true);
        helper.succeed();
    }

    private void verifyRemoved(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        helper.assertTrue(claims(chunk(level, MANUAL)).getOwningColony() == 0, "Unclaim lost, old owner resurrected");
        helper.assertTrue(!claims(chunk(level, MANUAL)).getStaticClaimColonies().contains(1), "Removed static claim resurrected");
        helper.assertTrue(claims(chunk(level, BUILDING)).getAllClaimingBuildings().isEmpty(), "Removed building claim resurrected");
        helper.assertTrue(IColonyManager.getInstance().getColonyByWorld(1, level).getName().equals("Claims restart newest"), "Latest mutation reverted to old backup");
        final ChunkPos removeLater = new ChunkPos(242, 240);
        final ChunkLoadStorage pending = queue(level).getAllChunkStorages().get(removeLater);
        helper.assertTrue(pending != null, "Unloaded building removal lost");
        final LevelChunk target = chunk(level, removeLater);
        claims(target).addBuildingClaim(1, BUILDING_ANCHOR, target);
        pending.applyToCap(claims(target), target);
        helper.assertTrue(claims(target).getAllClaimingBuildings().isEmpty(), "Pending building removal failed to deserialize");
        helper.succeed();
    }

    private void mixedRoundTrip(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final ChunkPos pos = new ChunkPos(245, 240);
        final LevelChunk target = chunk(level, pos);
        claims(target).reset(target);
        final ChunkLoadStorage mixed = new ChunkLoadStorage(1, pos.toLong(), true, level.dimension().location(), true);
        mixed.merge(new ChunkLoadStorage(1, pos.toLong(), level.dimension().location(), BUILDING_ANCHOR, true));
        final ChunkLoadStorage restored = new ChunkLoadStorage(mixed.toNBT());
        restored.applyToCap(claims(target), target);
        helper.assertTrue(claims(target).getStaticClaimColonies().contains(1), "Mixed queue lost static claim");
        helper.assertTrue(claims(target).getAllClaimingBuildings().getOrDefault(1, java.util.Set.of()).contains(BUILDING_ANCHOR),
          "Mixed queue lost building claim");
        restored.merge(new ChunkLoadStorage(1, pos.toLong(), level.dimension().location(), BUILDING_ANCHOR, false));
        new ChunkLoadStorage(restored.toNBT()).applyToCap(claims(target), target);
        helper.assertTrue(claims(target).getAllClaimingBuildings().isEmpty(), "Latest merged unclaim lost in round-trip");
        helper.succeed();
    }
}
