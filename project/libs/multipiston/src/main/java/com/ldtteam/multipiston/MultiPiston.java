package com.ldtteam.multipiston;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Registry;

/** Fabric entrypoint for the standalone Multi-Piston mod. */
public final class MultiPiston implements ModInitializer
{
    public static final String MOD_ID = "multipiston";

    @Override
    public void onInitialize()
    {
        ModBlocks.initialize();
        ModTileEntities.initialize();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("general"),
          CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
            .title(Component.translatable("block.multipiston.multipistonblock"))
            .icon(() -> new ItemStack(ModBlocks.MULTIPISTON))
            .displayItems((parameters, output) -> output.accept(ModBlocks.MULTIPISTON))
            .build());

        MultiPistonNetwork.registerServerReceiver();
    }

    public static ResourceLocation id(final String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }
}
