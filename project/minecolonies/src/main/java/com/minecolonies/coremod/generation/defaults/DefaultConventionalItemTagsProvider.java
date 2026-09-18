package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.fabric.common.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Supplies conventional item tags absent from Fabric API 0.92.2 but used by
 * the 1.20.1 MineColonies recipes and worker filters.
 */
public final class DefaultConventionalItemTagsProvider extends FabricTagProvider.ItemTagProvider
{
    public DefaultConventionalItemTagsProvider(
      @NotNull final FabricDataOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider)
    {
        getOrCreateTagBuilder(Tags.Items.CROPS)
          .add(
            Items.WHEAT,
            Items.CARROT,
            Items.POTATO,
            Items.BEETROOT,
            Items.PUMPKIN,
            Items.MELON,
            Items.COCOA_BEANS,
            Items.NETHER_WART,
            Items.SWEET_BERRIES,
            Items.GLOW_BERRIES,
            Items.CHORUS_FRUIT,
            Items.TORCHFLOWER,
            Items.PITCHER_PLANT);

        getOrCreateTagBuilder(Tags.Items.CROPS_WHEAT).add(Items.WHEAT);

        getOrCreateTagBuilder(Tags.Items.SEEDS)
          .add(
            Items.WHEAT_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.MELON_SEEDS,
            Items.TORCHFLOWER_SEEDS,
            Items.PITCHER_POD);

        getOrCreateTagBuilder(Tags.Items.EGGS).add(Items.EGG, Items.TURTLE_EGG, Items.SNIFFER_EGG);
        getOrCreateTagBuilder(Tags.Items.STRING).add(Items.STRING);

        getOrCreateTagBuilder(Tags.Items.GLASS)
          .add(
            Items.GLASS,
            Items.TINTED_GLASS,
            Items.WHITE_STAINED_GLASS,
            Items.ORANGE_STAINED_GLASS,
            Items.MAGENTA_STAINED_GLASS,
            Items.LIGHT_BLUE_STAINED_GLASS,
            Items.YELLOW_STAINED_GLASS,
            Items.LIME_STAINED_GLASS,
            Items.PINK_STAINED_GLASS,
            Items.GRAY_STAINED_GLASS,
            Items.LIGHT_GRAY_STAINED_GLASS,
            Items.CYAN_STAINED_GLASS,
            Items.PURPLE_STAINED_GLASS,
            Items.BLUE_STAINED_GLASS,
            Items.BROWN_STAINED_GLASS,
            Items.GREEN_STAINED_GLASS,
            Items.RED_STAINED_GLASS,
            Items.BLACK_STAINED_GLASS);

        getOrCreateTagBuilder(Tags.Items.DUSTS_REDSTONE).add(Items.REDSTONE);
        getOrCreateTagBuilder(Tags.Items.ORES_REDSTONE).add(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE);
        getOrCreateTagBuilder(Tags.Items.STORAGE_BLOCKS_REDSTONE).add(Items.REDSTONE_BLOCK);

        getOrCreateTagBuilder(Tags.Items.STORAGE_BLOCKS)
          .add(
            Items.COAL_BLOCK,
            Items.IRON_BLOCK,
            Items.GOLD_BLOCK,
            Items.DIAMOND_BLOCK,
            Items.EMERALD_BLOCK,
            Items.REDSTONE_BLOCK,
            Items.LAPIS_BLOCK,
            Items.COPPER_BLOCK,
            Items.NETHERITE_BLOCK,
            Items.QUARTZ_BLOCK,
            Items.HAY_BLOCK,
            Items.DRIED_KELP_BLOCK,
            Items.BONE_BLOCK,
            Items.HONEY_BLOCK,
            Items.HONEYCOMB_BLOCK,
            Items.SNOW_BLOCK,
            Items.RAW_IRON_BLOCK,
            Items.RAW_GOLD_BLOCK,
            Items.RAW_COPPER_BLOCK);

        getOrCreateTagBuilder(Tags.Items.STONE)
          .add(
            Items.STONE,
            Items.GRANITE,
            Items.DIORITE,
            Items.ANDESITE,
            Items.DEEPSLATE,
            Items.TUFF,
            Items.CALCITE,
            Items.DRIPSTONE_BLOCK,
            Items.BLACKSTONE,
            Items.BASALT,
            Items.SMOOTH_BASALT,
            Items.NETHERRACK,
            Items.END_STONE,
            Items.PRISMARINE,
            Items.PRISMARINE_BRICKS,
            Items.DARK_PRISMARINE,
            Items.PURPUR_BLOCK,
            Items.QUARTZ_BLOCK);

        getOrCreateTagBuilder(Tags.Items.COBBLESTONE)
          .add(Items.COBBLESTONE, Items.MOSSY_COBBLESTONE, Items.COBBLED_DEEPSLATE, Items.BLACKSTONE);

        getOrCreateTagBuilder(Tags.Items.END_STONES).add(Items.END_STONE, Items.END_STONE_BRICKS);

        getOrCreateTagBuilder(Tags.Items.SANDSTONE)
          .add(
            Items.SANDSTONE,
            Items.CHISELED_SANDSTONE,
            Items.CUT_SANDSTONE,
            Items.SMOOTH_SANDSTONE,
            Items.RED_SANDSTONE,
            Items.CHISELED_RED_SANDSTONE,
            Items.CUT_RED_SANDSTONE,
            Items.SMOOTH_RED_SANDSTONE);

        getOrCreateTagBuilder(Tags.Items.GRAVEL).add(Items.GRAVEL);
        getOrCreateTagBuilder(Tags.Items.SAND).add(Items.SAND, Items.RED_SAND, Items.SOUL_SAND);
    }

    @Override
    public String getName()
    {
        return "MineColonies Conventional Item Tags";
    }
}
