package com.minecolonies.fabric.common;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import java.util.List;

public final class TierSortingRegistry
{
    private TierSortingRegistry()
    {
    }

    public static List<Tier> getSortedTiers()
    {
        return List.of(Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE);
    }
}
