package com.minecolonies.fabric.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.event.entity.player.ArrowLooseEvent;

/** Small compatibility surface for Forge hooks that have no direct Fabric event. */
public final class ForgeEventFactory
{
    private ForgeEventFactory()
    {
    }

    public static InteractionResultHolder<ItemStack> onArrowNock(final ItemStack stack, final Level level, final Player player, final InteractionHand hand, final boolean hasAmmo)
    {
        // Forge returns null when no ArrowNockEvent listener supplies a final
        // result.  ItemPharaoScepter uses that null value to continue into
        // startUsingItem; returning InteractionResultHolder.pass here would
        // incorrectly short-circuit the bow-use flow on Fabric.
        return null;
    }

    public static int onArrowLoose(final ItemStack stack, final Level level, final LivingEntity entity, final int charge, final boolean hasAmmo)
    {
        if (entity instanceof Player player)
        {
            final ArrowLooseEvent event = new ArrowLooseEvent(player);
            if (MinecraftForge.EVENT_BUS.post(event))
            {
                return -1;
            }
        }
        return charge;
    }

    public static boolean onProjectileImpact(final Entity projectile, final HitResult hitResult)
    {
        return false;
    }

    public static boolean canLivingConvert(final LivingEntity entity, final EntityType<?> targetType, final Object reason)
    {
        // Fabric's MOB_CONVERSION callback is dispatched before the candidate
        // entity is spawned.  No second cancellation gate is needed here; a
        // false default would permanently disable MineColonies' tavern visitor
        // conversion path.
        return true;
    }
}
