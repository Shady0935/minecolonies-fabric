package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.util.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Locale;
import java.util.function.Supplier;

/** Fabric/vanilla item registrations. */
public final class ModItems
{
    private ModItems() { }

    public static final RegistryEntry<ItemBuildTool> buildTool = register("sceptergold", () -> new ItemBuildTool(new Item.Properties()));
    public static final RegistryEntry<ItemShapeTool> shapeTool = register("shapetool", () -> new ItemShapeTool(new Item.Properties()));
    public static final RegistryEntry<ItemScanTool> scanTool = register("sceptersteel", ItemScanTool::new);
    public static final RegistryEntry<ItemTagTool> tagTool = register("sceptertag", ItemTagTool::new);
    public static final RegistryEntry<ItemCaliper> caliper = register("caliper", () -> new ItemCaliper(new Item.Properties()));
    public static final RegistryEntry<ItemTagSubstitution> blockTagSubstitution = register("blocktagsubstitution", () -> new ItemTagSubstitution(new Item.Properties()));

    public static void initialize()
    {
        // Static initialization performs the eager vanilla registrations.
    }

    public static <I extends Item> RegistryEntry<I> register(final String name, final Supplier<I> factory)
    {
        return new RegistryEntry<>(Registry.register(BuiltInRegistries.ITEM,
          new ResourceLocation(Constants.MOD_ID, name.toLowerCase(Locale.ROOT)), factory.get()));
    }

    public static RegistryEntry<BlockItem> registerBlockItem(final String name, final Block block)
    {
        return register(name, () -> new BlockItem(block, new Item.Properties()));
    }
}
