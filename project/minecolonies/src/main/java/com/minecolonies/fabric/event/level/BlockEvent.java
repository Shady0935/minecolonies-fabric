package com.minecolonies.fabric.event.level;

import com.minecolonies.fabric.common.util.BlockSnapshot;
import com.minecolonies.fabric.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEvent extends Event
{
    private final LevelAccessor level;
    private final BlockPos pos;
    private final BlockState state;

    protected BlockEvent(final LevelAccessor level, final BlockPos pos, final BlockState state)
    {
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    public LevelAccessor getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public BlockState getState() { return state; }

    public static class BreakEvent extends BlockEvent
    {
        private final Player player;
        public BreakEvent(final LevelAccessor level, final BlockPos pos, final BlockState state, final Player player)
        {
            super(level, pos, state); this.player = player;
        }
        public Player getPlayer() { return player; }
        public Entity getEntity() { return player; }
    }

    public static class EntityPlaceEvent extends BlockEvent
    {
        private final Entity entity;
        private final BlockState placedBlock;
        public EntityPlaceEvent(final BlockSnapshot snapshot, final BlockState placedBlock, final Entity entity)
        {
            super(snapshot.getLevel(), snapshot.getPos(), placedBlock); this.entity = entity; this.placedBlock = placedBlock;
        }
        public Entity getEntity() { return entity; }
        public Player getPlayer() { return entity instanceof Player player ? player : null; }
        public BlockState getPlacedBlock() { return placedBlock; }
    }

    public static class FarmlandTrampleEvent extends BlockEvent
    {
        private final Entity entity;
        public FarmlandTrampleEvent(final Level level, final BlockPos pos, final BlockState state, final Entity entity)
        {
            super(level, pos, state); this.entity = entity;
        }
        public Entity getEntity() { return entity; }
    }
}
