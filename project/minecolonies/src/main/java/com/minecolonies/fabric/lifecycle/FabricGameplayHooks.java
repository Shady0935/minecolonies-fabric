package com.minecolonies.fabric.lifecycle;

import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.common.util.BlockSnapshot;
import com.minecolonies.fabric.event.Event;
import com.minecolonies.fabric.event.entity.living.LivingDeathEvent;
import com.minecolonies.fabric.event.entity.living.LivingHurtEvent;
import com.minecolonies.fabric.event.entity.living.LivingConversionEvent;
import com.minecolonies.fabric.event.entity.player.AttackEntityEvent;
import com.minecolonies.fabric.event.entity.player.FillBucketEvent;
import com.minecolonies.fabric.event.entity.player.PlayerInteractEvent;
import com.minecolonies.fabric.event.entity.player.PlayerEvent;
import com.minecolonies.fabric.event.level.BlockEvent;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Adapts Fabric's real gameplay callbacks to the retained MineColonies event
 * handlers.  This class deliberately contains no fallback behaviour: a
 * callback is registered only where Fabric exposes the corresponding point in
 * the 1.20.1 gameplay flow.
 */
public final class FabricGameplayHooks
{
    private static boolean registered;

    private FabricGameplayHooks()
    {
    }

    /** Register the common gameplay callbacks once. */
    public static void register()
    {
        if (registered)
        {
            return;
        }
        registered = true;

        UseBlockCallback.EVENT.register(FabricGameplayHooks::onUseBlock);
        UseItemCallback.EVENT.register(FabricGameplayHooks::onUseItem);
        UseEntityCallback.EVENT.register(FabricGameplayHooks::onUseEntity);
        AttackEntityCallback.EVENT.register(FabricGameplayHooks::onAttackEntity);
        PlayerBlockBreakEvents.BEFORE.register(FabricGameplayHooks::onBeforeBlockBreak);

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(FabricGameplayHooks::onPlayerChangedDimension);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(FabricGameplayHooks::onLivingHurt);
        ServerLivingEntityEvents.AFTER_DEATH.register(FabricGameplayHooks::onLivingDeath);
        ServerLivingEntityEvents.MOB_CONVERSION.register(FabricGameplayHooks::onMobConversion);
    }

    private static InteractionResult onUseBlock(final Player player, final Level level, final InteractionHand hand, final BlockHitResult hit)
    {
        final ItemStack stack = player.getItemInHand(hand);
        final PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, level, hand, stack, hit);
        MinecraftForge.EVENT_BUS.post(event);

        InteractionResult result = interactionResult(event);
        if (result != InteractionResult.PASS)
        {
            return result;
        }

        // Forge's EntityPlaceEvent is emitted after a valid placement context
        // is established.  Fabric has no matching 1.20.1 callback, so use the
        // same placement context here before vanilla performs the placement.
        if (!level.isClientSide() && stack.getItem() instanceof BlockItem blockItem)
        {
            final BlockPlaceContext placementContext = new BlockPlaceContext(player, hand, stack, hit);
            final BlockState placedState = placementContext.canPlace()
              ? blockItem.getBlock().getStateForPlacement(placementContext)
              : null;
            if (placedState != null)
            {
                // BlockPlaceContext.getClickedPos() already resolves the
                // replace-vs-adjacent placement position. Do not offset it a
                // second time, otherwise colony permission checks target the
                // wrong block for ordinary placement.
                final BlockPos placedPos = placementContext.getClickedPos();
                final BlockEvent.EntityPlaceEvent placeEvent = new BlockEvent.EntityPlaceEvent(
                  BlockSnapshot.create(level.dimension(), level, placedPos), placedState, player);
                MinecraftForge.EVENT_BUS.post(placeEvent);
                if (placeEvent.isCanceled() || placeEvent.getResult() == Event.Result.DENY)
                {
                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.PASS;
    }

    private static InteractionResultHolder<ItemStack> onUseItem(final Player player, final Level level, final InteractionHand hand)
    {
        final ItemStack stack = player.getItemInHand(hand);
        final PlayerInteractEvent.RightClickItem event = new PlayerInteractEvent.RightClickItem(player, level, hand, stack);
        MinecraftForge.EVENT_BUS.post(event);
        final InteractionResult result = interactionResult(event);
        if (result == InteractionResult.PASS)
        {
            if (stack.getItem() instanceof BucketItem)
            {
                final HitResult target = player.pick(5.0D, 0.0F, false);
                final FillBucketEvent fillBucketEvent = new FillBucketEvent(player, target);
                MinecraftForge.EVENT_BUS.post(fillBucketEvent);
                if (fillBucketEvent.isCanceled() || fillBucketEvent.getResult() == Event.Result.DENY)
                {
                    return InteractionResultHolder.fail(stack);
                }
            }
            return InteractionResultHolder.pass(stack);
        }
        if (result == InteractionResult.SUCCESS || result == InteractionResult.CONSUME || result == InteractionResult.CONSUME_PARTIAL)
        {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private static InteractionResult onUseEntity(final Player player, final Level level, final InteractionHand hand,
      final Entity target, final EntityHitResult hit)
    {
        // Posting the specific subtype preserves Forge's normal assignability:
        // subscribers of both EntityInteract and EntityInteractSpecific receive
        // the same interaction exactly once through the local event bus.
        final PlayerInteractEvent.EntityInteractSpecific event =
          new PlayerInteractEvent.EntityInteractSpecific(player, level, hand, target);
        MinecraftForge.EVENT_BUS.post(event);
        return interactionResult(event);
    }

    private static InteractionResult onAttackEntity(final Player player, final Level level, final InteractionHand hand,
      final Entity target, final EntityHitResult hit)
    {
        final AttackEntityEvent event = new AttackEntityEvent(player, target);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS;
    }

    private static boolean onBeforeBlockBreak(final Level level, final Player player, final BlockPos pos,
      final BlockState state, final net.minecraft.world.level.block.entity.BlockEntity blockEntity)
    {
        final BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled() && event.getResult() != Event.Result.DENY;
    }

    private static void onPlayerChangedDimension(final net.minecraft.server.level.ServerPlayer player,
      final net.minecraft.server.level.ServerLevel from, final net.minecraft.server.level.ServerLevel to)
    {
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(player, from.dimension(), to.dimension()));
    }

    private static boolean onLivingHurt(final LivingEntity entity, final net.minecraft.world.damagesource.DamageSource source,
      final float amount)
    {
        final LivingHurtEvent event = new LivingHurtEvent(entity, source);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled() && event.getResult() != Event.Result.DENY;
    }

    private static void onLivingDeath(final LivingEntity entity, final net.minecraft.world.damagesource.DamageSource source)
    {
        MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity));
    }

    /**
     * Fabric calls this immediately before the converted mob is spawned.  That
     * is the closest 1.20.1 equivalent to Forge's conversion pre-event: post
     * the retained event first, then discard Fabric's candidate if the
     * MineColonies handler replaced the conversion with a visitor.
     */
    private static void onMobConversion(final net.minecraft.world.entity.Mob previous,
      final net.minecraft.world.entity.Mob converted, final boolean keepEquipment)
    {
        final LivingConversionEvent.Pre event = new LivingConversionEvent.Pre(previous, converted.getType());
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled() && !converted.isRemoved())
        {
            converted.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private static InteractionResult interactionResult(final com.minecolonies.fabric.event.entity.player.PlayerInteractEvent event)
    {
        if (event.isCanceled())
        {
            return event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult();
        }
        if (event.getResult() == Event.Result.DENY)
        {
            return InteractionResult.FAIL;
        }
        if (event.getResult() == Event.Result.ALLOW)
        {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
