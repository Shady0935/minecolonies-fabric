package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.fabric.common.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Supplies the small set of conventional block tags used by MineColonies
 * which is not provided by Fabric API 0.92.2.
 */
public final class DefaultConventionalBlockTagsProvider extends FabricTagProvider.BlockTagProvider
{
    public DefaultConventionalBlockTagsProvider(
      @NotNull final FabricDataOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider)
    {
        getOrCreateTagBuilder(Tags.Blocks.STONE)
          .add(
            Blocks.STONE,
            Blocks.GRANITE,
            Blocks.DIORITE,
            Blocks.ANDESITE,
            Blocks.DEEPSLATE,
            Blocks.TUFF,
            Blocks.CALCITE,
            Blocks.DRIPSTONE_BLOCK,
            Blocks.BLACKSTONE,
            Blocks.BASALT,
            Blocks.SMOOTH_BASALT,
            Blocks.NETHERRACK,
            Blocks.END_STONE,
            Blocks.PRISMARINE,
            Blocks.PRISMARINE_BRICKS,
            Blocks.DARK_PRISMARINE,
            Blocks.PURPUR_BLOCK,
            Blocks.QUARTZ_BLOCK);

        getOrCreateTagBuilder(Tags.Blocks.COBBLESTONE)
          .add(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.COBBLED_DEEPSLATE, Blocks.BLACKSTONE);

        getOrCreateTagBuilder(Tags.Blocks.STORAGE_BLOCKS)
          .add(
            Blocks.COAL_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.DIAMOND_BLOCK,
            Blocks.EMERALD_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.LAPIS_BLOCK,
            Blocks.COPPER_BLOCK,
            Blocks.NETHERITE_BLOCK,
            Blocks.QUARTZ_BLOCK,
            Blocks.HAY_BLOCK,
            Blocks.DRIED_KELP_BLOCK,
            Blocks.BONE_BLOCK,
            Blocks.HONEY_BLOCK,
            Blocks.HONEYCOMB_BLOCK,
            Blocks.SNOW_BLOCK,
            Blocks.RAW_IRON_BLOCK,
            Blocks.RAW_GOLD_BLOCK,
            Blocks.RAW_COPPER_BLOCK);
    }

    @Override
    public String getName()
    {
        return "MineColonies Conventional Block Tags";
    }
}
