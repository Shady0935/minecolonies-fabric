package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.util.RegistryEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** Structurize creative tab on Fabric. */
public final class ModItemGroups
{
    private ModItemGroups() { }

    public static final RegistryEntry<CreativeModeTab> GENERAL = new RegistryEntry<>(Registry.register(
      BuiltInRegistries.CREATIVE_MODE_TAB,
      new ResourceLocation(Constants.MOD_ID, "general"),
      FabricItemGroup.builder()
        .icon(() -> new ItemStack(ModItems.buildTool.get()))
        .title(Component.translatable("itemGroup." + Constants.MOD_ID))
        .displayItems((context, output) -> {
            output.accept(ModBlocks.blockSubstitution.get());
            output.accept(ModBlocks.blockSolidSubstitution.get());
            output.accept(ModBlocks.blockFluidSubstitution.get());
            output.accept(ModItems.buildTool.get());
            output.accept(ModItems.shapeTool.get());
            output.accept(ModItems.scanTool.get());
            output.accept(ModItems.tagTool.get());
            output.accept(ModItems.caliper.get());
            output.accept(ModItems.blockTagSubstitution.get());
        })
        .build()));

    public static void initialize()
    {
        // Static initialization performs the eager vanilla registration.
    }
}
