package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Fabric-side equivalent of Forge's result-bearing bow-nock event. */
public class ArrowNockEvent extends Event
{
    private final Player entity;
    private final ItemStack bow;
    private final InteractionHand hand;
    private final Level level;
    private final boolean hasAmmo;
    private InteractionResultHolder<ItemStack> action;

    public ArrowNockEvent(final Player entity, final ItemStack bow, final InteractionHand hand,
      final Level level, final boolean hasAmmo)
    {
        this.entity = entity;
        this.bow = bow;
        this.hand = hand;
        this.level = level;
        this.hasAmmo = hasAmmo;
    }

    public Player getEntity()
    {
        return entity;
    }

    public ItemStack getBow()
    {
        return bow;
    }

    public InteractionHand getHand()
    {
        return hand;
    }

    public Level getLevel()
    {
        return level;
    }

    public boolean hasAmmo()
    {
        return hasAmmo;
    }

    public InteractionResultHolder<ItemStack> getAction()
    {
        return action;
    }

    public void setAction(final InteractionResultHolder<ItemStack> action)
    {
        this.action = action;
    }
}
