package com.ldtteam.domumornamentum.entity.block;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Eager Fabric block-entity type registrations. */
public final class ModBlockEntityTypes
{
    public static final Supplier<BlockEntityType<BlockEntity>> MATERIALLY_TEXTURED = register(
        Constants.BlockEntityTypes.MATERIALLY_RETEXTURABLE, MateriallyTexturedBlockEntity::new,
        IMateriallyTexturedBlock.class);

    /* The 1.20.1 source does not ship the later dynamic timber-frame block. */

    private ModBlockEntityTypes()
    {
    }

    public static void init()
    {
    }

    private static Supplier<BlockEntityType<BlockEntity>> register(final String name,
                                                                     final BlockEntityType.BlockEntitySupplier<BlockEntity> factory,
                                                                     final Class<?> validBlockType)
    {
        final Set<Block> validBlocks = BuiltInRegistries.BLOCK.stream()
            .filter(validBlockType::isInstance)
            .collect(Collectors.toSet());
        final BlockEntityType<BlockEntity> value = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(Constants.MOD_ID, name),
            BlockEntityType.Builder.of(factory, validBlocks.toArray(Block[]::new)).build(null));
        return () -> value;
    }
}
