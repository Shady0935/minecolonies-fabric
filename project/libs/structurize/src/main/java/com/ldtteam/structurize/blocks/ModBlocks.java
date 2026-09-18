package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.schematic.BlockFluidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockTagSubstitution;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.util.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import java.util.Locale;
import java.util.function.Supplier;

/** Fabric/vanilla block registrations. */
public final class ModBlocks
{
    private ModBlocks() { }

    public static final TagKey<Block> NULL_PLACEMENT = TagKey.create(Registries.BLOCK, new ResourceLocation(Constants.MOD_ID, "null_placement"));

    // This block must exist before ModItems is initialized because its item
    // constructor resolves the block entry during static initialization.
    public static final RegistryEntry<BlockTagSubstitution> blockTagSubstitution = registerBlock("blocktagsubstitution", BlockTagSubstitution::new);
    public static final RegistryEntry<BlockSubstitution> blockSubstitution = registerWithItem("blocksubstitution", BlockSubstitution::new);
    public static final RegistryEntry<BlockSolidSubstitution> blockSolidSubstitution = registerWithItem("blocksolidsubstitution", BlockSolidSubstitution::new);
    public static final RegistryEntry<BlockFluidSubstitution> blockFluidSubstitution = registerWithItem("blockfluidsubstitution", BlockFluidSubstitution::new);

    public static void initialize()
    {
        // Static initialization performs the eager vanilla registrations.
    }

    private static <B extends Block> RegistryEntry<B> registerWithItem(final String name, final Supplier<B> factory)
    {
        final RegistryEntry<B> block = registerBlock(name, factory);
        ModItems.registerBlockItem(name, block.get());
        return block;
    }

    private static <B extends Block> RegistryEntry<B> registerBlock(final String name, final Supplier<B> factory)
    {
        final String normalized = name.toLowerCase(Locale.ROOT);
        return new RegistryEntry<>(Registry.register(BuiltInRegistries.BLOCK,
          new ResourceLocation(Constants.MOD_ID, normalized), factory.get()));
    }
}
