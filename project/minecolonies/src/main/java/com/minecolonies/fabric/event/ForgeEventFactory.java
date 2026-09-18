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

/** Small compatibility surface for Forge hooks that have no direct Fabric event. */
public final class ForgeEventFactory
{
    private ForgeEventFactory()
    {
    }

    public static InteractionResultHolder<ItemStack> onArrowNock(final ItemStack stack, final Level level, final Player player, final InteractionHand hand, final boolean hasAmmo)
    {
        return InteractionResultHolder.pass(stack);
    }

    public static int onArrowLoose(final ItemStack stack, final Level level, final LivingEntity entity, final int charge, final boolean hasAmmo)
    {
        return charge;
    }

    public static boolean onProjectileImpact(final Entity projectile, final HitResult hitResult)
    {
        return false;
    }

    public static boolean canLivingConvert(final LivingEntity entity, final EntityType<?> targetType, final Object reason)
    {
        return false;
    }
}
