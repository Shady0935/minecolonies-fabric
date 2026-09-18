package com.minecolonies.fabric.common;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class Tags
{
    private Tags()
    {
    }

    private static TagKey<Item> itemTag(final String path)
    {
        return TagKey.create(Registries.ITEM, new ResourceLocation("c", path));
    }

    private static TagKey<Block> blockTag(final String path)
    {
        return TagKey.create(Registries.BLOCK, new ResourceLocation("c", path));
    }

    public static final class Items
    {
        public static final TagKey<Item> CHESTS = itemTag("chests");
        public static final TagKey<Item> COBBLESTONE = itemTag("cobblestone");
        public static final TagKey<Item> CROPS = itemTag("crops");
        public static final TagKey<Item> CROPS_WHEAT = itemTag("crops/wheat");
        public static final TagKey<Item> DUSTS = itemTag("dusts");
        public static final TagKey<Item> DUSTS_REDSTONE = itemTag("dusts/redstone");
        public static final TagKey<Item> DYES = itemTag("dyes");
        public static final TagKey<Item> EGGS = itemTag("eggs");
        public static final TagKey<Item> END_STONES = itemTag("end_stones");
        public static final TagKey<Item> GEMS = itemTag("gems");
        public static final TagKey<Item> GLASS = itemTag("glass");
        public static final TagKey<Item> GLASS_PANES = itemTag("glass_panes");
        public static final TagKey<Item> GRAVEL = itemTag("gravel");
        public static final TagKey<Item> HARVEST = itemTag("harvest");
        public static final TagKey<Item> INGOTS = itemTag("ingots");
        public static final TagKey<Item> NUGGETS = itemTag("nuggets");
        public static final TagKey<Item> ORES = itemTag("ores");
        public static final TagKey<Item> ORES_REDSTONE = itemTag("ores/redstone");
        public static final TagKey<Item> SAND = itemTag("sand");
        public static final TagKey<Item> SANDSTONE = itemTag("sandstone");
        public static final TagKey<Item> SEEDS = itemTag("seeds");
        public static final TagKey<Item> STONE = itemTag("stone");
        public static final TagKey<Item> STORAGE_BLOCKS = itemTag("storage_blocks");
        public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = itemTag("storage_blocks/redstone");
        public static final TagKey<Item> STRING = itemTag("string");
        public static final TagKey<Item> SWORD = itemTag("sword");
    }

    public static final class Blocks
    {
        public static final TagKey<Block> BOOKSHELVES = blockTag("bookshelves");
        public static final TagKey<Block> COBBLESTONE = blockTag("cobblestone");
        public static final TagKey<Block> SLIMY_GRASS = blockTag("slimy_grass");
        public static final TagKey<Block> SLIMY_LEAVES = blockTag("slimy_leaves");
        public static final TagKey<Block> SLIMY_LOGS = blockTag("slimy_logs");
        public static final TagKey<Block> SLIMY_SAPLINGS = blockTag("slimy_saplings");
        public static final TagKey<Block> STONE = blockTag("stone");
        public static final TagKey<Block> STORAGE_BLOCKS = blockTag("storage_blocks");
    }
}
