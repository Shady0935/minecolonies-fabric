package com.ldtteam.multipiston;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Registry for Multi-Piston block entities. */
public final class ModTileEntities
{
    public static final BlockEntityType<TileEntityMultiPiston> MULTIPISTON = Registry.register(
      BuiltInRegistries.BLOCK_ENTITY_TYPE,
      MultiPiston.id("multipistonte"),
      FabricBlockEntityTypeBuilder.create(TileEntityMultiPiston::new, ModBlocks.MULTIPISTON).build());

    private ModTileEntities()
    {
    }

    public static void initialize()
    {
        // Trigger static registration during common initialization.
    }
}
