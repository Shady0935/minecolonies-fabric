package com.minecolonies.fabric.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableLoadEvent extends Event
{
    private final ResourceLocation name;
    private final LootTable table;

    public LootTableLoadEvent(final ResourceLocation name, final LootTable table)
    {
        this.name = name;
        this.table = table;
    }

    public ResourceLocation getName() { return name; }
    public LootTable getTable() { return table; }
}
