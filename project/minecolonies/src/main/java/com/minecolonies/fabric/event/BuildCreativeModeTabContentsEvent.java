package com.minecolonies.fabric.event;

import com.minecolonies.fabric.common.util.MutableHashedLinkedMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class BuildCreativeModeTabContentsEvent extends Event
{
    private final CreativeModeTab tab;
    private final ResourceKey<CreativeModeTab> tabKey;
    private final CreativeModeTab.ItemDisplayParameters parameters;
    private final MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries;

    public BuildCreativeModeTabContentsEvent(final CreativeModeTab tab,
                                             final ResourceKey<CreativeModeTab> tabKey,
                                             final CreativeModeTab.ItemDisplayParameters parameters,
                                             final MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries)
    {
        this.tab = tab;
        this.tabKey = tabKey;
        this.parameters = parameters;
        this.entries = entries;
    }

    public CreativeModeTab getTab() { return tab; }
    public ResourceKey<CreativeModeTab> getTabKey() { return tabKey; }
    public CreativeModeTab.ItemDisplayParameters getParameters() { return parameters; }
    public MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> getEntries() { return entries; }
}
