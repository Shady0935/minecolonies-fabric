package com.ldtteam.multipiston;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/** Registry handles for Multi-Piston content. */
public final class ModBlocks
{
    public static final MultiPistonBlock MULTIPISTON = Registry.register(
      BuiltInRegistries.BLOCK,
      MultiPiston.id("multipistonblock"),
      new MultiPistonBlock());

    public static final BlockItem MULTIPISTON_ITEM = Registry.register(
      BuiltInRegistries.ITEM,
      MultiPiston.id("multipistonblock"),
      new BlockItem(MULTIPISTON, new Item.Properties()));

    private ModBlocks()
    {
    }

    public static void initialize()
    {
        // Trigger static registration before block entity and tab setup.
    }
}
