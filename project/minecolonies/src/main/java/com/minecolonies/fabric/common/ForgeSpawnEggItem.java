package com.minecolonies.fabric.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Supplier;

public class ForgeSpawnEggItem extends SpawnEggItem
{
    public ForgeSpawnEggItem(final Supplier<? extends EntityType<? extends Mob>> type,
                             final int primaryColor,
                             final int secondaryColor,
                             final Properties properties)
    {
        super(type.get(), primaryColor, secondaryColor, properties);
    }
}
