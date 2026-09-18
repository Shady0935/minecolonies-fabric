package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.fabric.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;

public class PlayerInteractEvent extends Event
{
    private final Player entity;
    private final Level level;
    private final InteractionHand hand;
    private final ItemStack itemStack;
    private final BlockPos pos;
    private final Direction face;
    private final HitResult target;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    protected PlayerInteractEvent(final Player entity, final Level level, final InteractionHand hand, final ItemStack itemStack, final BlockPos pos, final Direction face, final HitResult target)
    {
        this.entity = entity; this.level = level; this.hand = hand; this.itemStack = itemStack; this.pos = pos; this.face = face; this.target = target;
    }
    public Player getEntity() { return entity; }
    public Level getLevel() { return level; }
    public InteractionHand getHand() { return hand; }
    public ItemStack getItemStack() { return itemStack; }
    public BlockPos getPos() { return pos; }
    public Direction getFace() { return face; }
    public HitResult getTarget() { return target; }
    public void setCancellationResult(final net.minecraft.world.InteractionResult result) { cancellationResult = result; }
    public net.minecraft.world.InteractionResult getCancellationResult() { return cancellationResult; }

    public static class RightClickBlock extends PlayerInteractEvent
    {
        public RightClickBlock(final Player player, final Level level, final InteractionHand hand, final ItemStack stack, final BlockHitResult hit)
        { super(player, level, hand, stack, hit.getBlockPos(), hit.getDirection(), hit); }
    }

    public static class RightClickItem extends PlayerInteractEvent
    {
        public RightClickItem(final Player player, final Level level, final InteractionHand hand, final ItemStack stack)
        { super(player, level, hand, stack, player.blockPosition(), Direction.UP, BlockHitResult.miss(player.getEyePosition(), Direction.UP, player.blockPosition())); }
    }

    public static class EntityInteract extends PlayerInteractEvent
    {
        private final Entity targetEntity;
        public EntityInteract(final Player player, final Level level, final InteractionHand hand, final Entity target)
        { super(player, level, hand, player.getItemInHand(hand), target.blockPosition(), Direction.UP, new EntityHitResult(target)); this.targetEntity = target; }
        public Entity getTargetEntity() { return targetEntity; }
    }

    public static class EntityInteractSpecific extends EntityInteract
    {
        public EntityInteractSpecific(final Player player, final Level level, final InteractionHand hand, final Entity target) { super(player, level, hand, target); }
    }

}
