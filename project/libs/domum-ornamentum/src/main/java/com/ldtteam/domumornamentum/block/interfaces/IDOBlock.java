package com.ldtteam.domumornamentum.block.interfaces;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

public interface IDOBlock<B extends IDOBlock<B>>
{
    default ResourceLocation getRegistryName(final Block block)
    {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    ResourceLocation getRegistryName();
}
