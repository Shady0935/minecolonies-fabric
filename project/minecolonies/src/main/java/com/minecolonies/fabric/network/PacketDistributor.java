package com.minecolonies.fabric.network;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.Supplier;

/** Target descriptors retained for the existing MineColonies send API. */
public final class PacketDistributor
{
    public static final TargetFactory<ServerPlayer> PLAYER = new TargetFactory<>(TargetKind.PLAYER);
    public static final TargetFactory<ResourceKey<Level>> DIMENSION = new TargetFactory<>(TargetKind.DIMENSION);
    public static final TargetFactory<TargetPoint> NEAR = new TargetFactory<>(TargetKind.NEAR);
    public static final TargetFactory<Entity> TRACKING_ENTITY = new TargetFactory<>(TargetKind.TRACKING_ENTITY);
    public static final TargetFactory<Entity> TRACKING_ENTITY_AND_SELF = new TargetFactory<>(TargetKind.TRACKING_ENTITY_AND_SELF);
    public static final TargetFactory<LevelChunk> TRACKING_CHUNK = new TargetFactory<>(TargetKind.TRACKING_CHUNK);
    public static final TargetFactory<Object> ALL = new TargetFactory<>(TargetKind.ALL);

    private PacketDistributor()
    {
    }

    public static final class PacketTarget
    {
        private final Object value;
        private final TargetKind kind;

        private PacketTarget(final Object value, final TargetKind kind)
        {
            this.value = value;
            this.kind = kind;
        }

        public Object value()
        {
            return value;
        }

        public TargetKind kind()
        {
            return kind;
        }
    }

    public static final class TargetFactory<T>
    {
        private final TargetKind kind;

        private TargetFactory(final TargetKind kind)
        {
            this.kind = kind;
        }

        public PacketTarget with(final Supplier<T> supplier)
        {
            return new PacketTarget(supplier.get(), kind);
        }

        public PacketTarget noArg()
        {
            return new PacketTarget(null, kind);
        }
    }

    public enum TargetKind
    {
        PLAYER,
        DIMENSION,
        NEAR,
        TRACKING_ENTITY,
        TRACKING_ENTITY_AND_SELF,
        TRACKING_CHUNK,
        ALL
    }

    public static final class TargetPoint
    {
        public final double x;
        public final double y;
        public final double z;
        public final double range;
        public final ResourceKey<Level> dimension;

        public TargetPoint(final double x, final double y, final double z, final double range, final ResourceKey<Level> dimension)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.range = range;
            this.dimension = dimension;
        }
    }
}
