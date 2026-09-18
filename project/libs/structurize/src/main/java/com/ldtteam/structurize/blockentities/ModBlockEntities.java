package com.ldtteam.structurize.blockentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.util.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities
{
    private ModBlockEntities() { }

    public static final RegistryEntry<BlockEntityType<BlockEntityTagSubstitution>> TAG_SUBSTITUTION = new RegistryEntry<>(
      Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
        new ResourceLocation(Constants.MOD_ID, "tagsubstitution"),
        BlockEntityType.Builder.of(BlockEntityTagSubstitution::new, ModBlocks.blockTagSubstitution.get()).build(null)));

    public static void initialize()
    {
        // Static initialization performs the eager vanilla registration.
    }
}
