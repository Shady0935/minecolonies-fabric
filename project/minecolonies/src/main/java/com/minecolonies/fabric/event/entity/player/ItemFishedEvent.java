package com.minecolonies.fabric.event.entity.player;

import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.fabric.event.Event;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Fabric-side equivalent of Forge's cancellable fishing-loot event. */
public class ItemFishedEvent extends Event
{
    private final NonNullList<ItemStack> drops = NonNullList.create();
    private final NewBobberEntity hook;
    private int rodDamage;

    public ItemFishedEvent(final List<ItemStack> drops, final int rodDamage, final NewBobberEntity hook)
    {
        this.drops.addAll(drops);
        this.rodDamage = rodDamage;
        this.hook = hook;
    }

    public int getRodDamage()
    {
        return rodDamage;
    }

    public void damageRodBy(final int rodDamage)
    {
        if (rodDamage < 0)
        {
            throw new IllegalArgumentException("Fishing rod damage must be non-negative");
        }
        this.rodDamage = rodDamage;
    }

    public NonNullList<ItemStack> getDrops()
    {
        return drops;
    }

    public NewBobberEntity getHookEntity()
    {
        return hook;
    }
}
