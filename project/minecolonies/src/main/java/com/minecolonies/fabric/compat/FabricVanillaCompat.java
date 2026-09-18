package com.minecolonies.fabric.compat;

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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.List;

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
        try
        {
            final var field = BrewingStandBlockEntity.class.getDeclaredField("brewTime");
            field.setAccessible(true);
            return field.getInt(brewingStand);
        }
        catch (ReflectiveOperationException ignored)
        {
            return 0;
        }
    }

    public static float getStepHeight(final Mob mob)
    {
        final Float value = readField(mob, "maxUpStep", Float.class);
        return value == null ? 0.6F : value;
    }

    public static boolean isAdvancementDone(final ClientAdvancements advancements, final Advancement advancement)
    {
        final Map<Advancement, AdvancementProgress> progress = readField(advancements, "progress", Map.class);
        return progress != null && progress.getOrDefault(advancement, new AdvancementProgress()).isDone();
    }

    public static Vec3 getExplosionPosition(final Explosion explosion)
    {
        try
        {
            final var method = Explosion.class.getDeclaredMethod("getPosition");
            method.setAccessible(true);
            return (Vec3) method.invoke(explosion);
        }
        catch (ReflectiveOperationException ignored)
        {
            return Vec3.ZERO;
        }
    }

    public static void setSpawnerRequiredPlayerRange(final BaseSpawner spawner, final int range)
    {
        writeField(spawner, "requiredPlayerRange", range);
    }

    public static CompoundTag getSpawnerEntityTag(final BaseSpawner spawner)
    {
        final SpawnData data = readField(spawner, "nextSpawnData", SpawnData.class);
        return data == null ? new CompoundTag() : data.getEntityToSpawn();
    }

    @SuppressWarnings("unchecked")
    public static List<Entity> releaseAllOccupants(final BeehiveBlockEntity hive, final BlockState state,
                                                    final BeehiveBlockEntity.BeeReleaseStatus status)
    {
        try
        {
            final var release = BeehiveBlockEntity.class.getDeclaredMethod("releaseAllOccupants", BlockState.class,
              BeehiveBlockEntity.BeeReleaseStatus.class);
            release.setAccessible(true);
            final Object result = release.invoke(hive, state, status);
            return result instanceof List<?> list ? (List<Entity>) list : List.of();
        }
        catch (ReflectiveOperationException ignored)
        {
            hive.emptyAllLivingFromHive(null, state, status);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public static ChunkHolder getVisibleChunk(final ServerChunkCache source, final long key)
    {
        final Map<Long, ChunkHolder> visible = readField(source.chunkMap, "visibleChunkMap", Map.class);
        return visible == null ? null : visible.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Set<Long> getVisibleChunkKeys(final ServerChunkCache source)
    {
        final Map<Long, ChunkHolder> visible = readField(source.chunkMap, "visibleChunkMap", Map.class);
        return visible == null ? Set.of() : visible.keySet();
    }

    @SuppressWarnings("unchecked")
    public static SortedArraySet<Ticket<?>> getTickets(final DistanceManager distanceManager, final long key)
    {
        final Map<Long, SortedArraySet<Ticket<?>>> tickets = readField(distanceManager, "tickets", Map.class);
        return tickets == null ? null : tickets.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Set<Mob> navigatingMobs(final ServerLevel level)
    {
        final Set<Mob> mobs = readField(level, "navigatingMobs", Set.class);
        return mobs == null ? Set.of() : mobs;
    }

    /**
     * Installs the suppliers collected by the retained Forge-style attribute event
     * into vanilla's 1.20.1 attribute table.
     */
    @SuppressWarnings("unchecked")
    public static void registerDefaultAttributes(final Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes)
    {
        if (attributes == null || attributes.isEmpty())
        {
            return;
        }

        try
        {
            final Field field = net.minecraft.world.entity.ai.attributes.DefaultAttributes.class.getDeclaredField("SUPPLIERS");
            field.setAccessible(true);
            final Object value = field.get(null);
            if (value instanceof Map<?, ?> suppliers)
            {
                ((Map<EntityType<? extends LivingEntity>, AttributeSupplier>) suppliers).putAll(attributes);
            }
        }
        catch (ReflectiveOperationException ignored)
        {
            // Mapping/access-transformer variants may expose a different field name.
        }
    }

    private static <T> T readField(final Object target, final String name, final Class<T> type)
    {
        if (target == null)
        {
            return null;
        }
        try
        {
            final Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            final Object value = field.get(target);
            return type.isInstance(value) ? type.cast(value) : null;
        }
        catch (ReflectiveOperationException ignored)
        {
            return null;
        }
    }

    private static void writeField(final Object target, final String name, final Object value)
    {
        if (target == null)
        {
            return;
        }
        try
        {
            final Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        }
        catch (ReflectiveOperationException ignored)
        {
            // Access transformers/mappings may expose this field directly on another runtime.
        }
    }
}
