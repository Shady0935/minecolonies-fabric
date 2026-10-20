package com.minecolonies.fabric.compat;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;


import java.util.Map;
import java.util.Set;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/** Small mappings for Forge convenience hooks absent from the 1.20.1 API. */
public final class FabricVanillaCompat
{
    private FabricVanillaCompat()
    {
    }

    public static boolean isLadder(final BlockState state, final LevelReader level, final BlockPos pos, final Entity entity)
    {
        return state != null && state.is(BlockTags.CLIMBABLE);
    }

    public static boolean isBed(final BlockState state, final Level level, final BlockPos pos, final Entity entity)
    {
        return state != null && state.getBlock() instanceof BedBlock;
    }

    public static boolean isFurnaceLit(final FurnaceBlockEntity furnace)
    {
        return furnace != null && furnace.getBlockState().hasProperty(FurnaceBlock.LIT)
            && furnace.getBlockState().getValue(FurnaceBlock.LIT);
    }

    public static int getBrewingTime(final BrewingStandBlockEntity brewingStand)
    {
        return brewingStand == null ? 0 : brewingStand.brewTime;
    }

    public static float getStepHeight(final Mob mob)
    {
        return mob == null ? 0.6F : mob.maxUpStep;
    }

    public static boolean isAdvancementDone(final ClientAdvancements advancements, final Advancement advancement)
    {
        final AdvancementProgress progress = advancements.progress.get(advancement);
        return progress != null && progress.isDone();
    }

    public static Vec3 getExplosionPosition(final Explosion explosion)
    {
        return explosion == null ? Vec3.ZERO : new Vec3(explosion.x, explosion.y, explosion.z);
    }

    public static void setSpawnerRequiredPlayerRange(final BaseSpawner spawner, final int range)
    {
        if (spawner != null) { spawner.requiredPlayerRange = range; }
    }

    public static CompoundTag getSpawnerEntityTag(final BaseSpawner spawner)
    {
        final SpawnData data = spawner == null ? null : spawner.nextSpawnData;
        return data == null ? new CompoundTag() : data.getEntityToSpawn();
    }

    @SuppressWarnings("unchecked")
    public static List<Entity> releaseAllOccupants(final BeehiveBlockEntity hive, final BlockState state,
                                                    final BeehiveBlockEntity.BeeReleaseStatus status)
    {
        return hive == null ? List.of() : hive.releaseAllOccupants(state, status);
    }

    public static ChunkHolder getVisibleChunk(final ServerChunkCache source, final long key)
    {
        // Direct references and the access widener are remapped by Loom. A
        // reflective named field lookup silently fails in intermediary jars.
        return source.chunkMap.visibleChunkMap.get(key);
    }

    public static Set<Long> getVisibleChunkKeys(final ServerChunkCache source)
    {
        return source.chunkMap.visibleChunkMap.keySet();
    }

    @SuppressWarnings("unchecked")
    public static SortedArraySet<Ticket<?>> getTickets(final DistanceManager distanceManager, final long key)
    {
        return distanceManager == null ? null : distanceManager.tickets.get(key);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Set<Mob> navigatingMobs(final ServerLevel level)
    {
        return level == null ? null : level.navigatingMobs;
    }

    /**
     * Installs the suppliers collected by the retained Forge-style attribute event
     * into vanilla's 1.20.1 attribute table.
     */
    public static void registerDefaultAttributes(final Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes)
    {
        if (attributes == null || attributes.isEmpty())
        {
            return;
        }

        attributes.forEach(FabricDefaultAttributeRegistry::register);
    }

}
