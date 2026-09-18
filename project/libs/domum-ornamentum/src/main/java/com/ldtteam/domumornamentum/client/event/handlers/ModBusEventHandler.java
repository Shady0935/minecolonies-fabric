package com.ldtteam.domumornamentum.client.event.handlers;

import com.ldtteam.domumornamentum.block.IModBlocks;
import com.ldtteam.domumornamentum.block.decorative.ExtraBlock;
import com.ldtteam.domumornamentum.block.types.DoorType;
import com.ldtteam.domumornamentum.block.types.FancyDoorType;
import com.ldtteam.domumornamentum.block.types.FancyTrapdoorType;
import com.ldtteam.domumornamentum.block.types.PostType;
import com.ldtteam.domumornamentum.block.types.TrapdoorType;
import com.ldtteam.domumornamentum.client.screens.ArchitectsCutterScreen;
import com.ldtteam.domumornamentum.container.ModContainerTypes;
import com.ldtteam.domumornamentum.shingles.ShingleHeightType;
import com.ldtteam.domumornamentum.util.Constants;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Client-only registrations replacing Forge's FML client setup event. */
public final class ModBusEventHandler
{
    private ModBusEventHandler()
    {
    }

    public static void registerClient()
    {
        final IModBlocks blocks = IModBlocks.getInstance();

        ItemProperties.register(blocks.getTrapdoor().asItem(), new ResourceLocation(Constants.TRAPDOOR_MODEL_OVERRIDE),
            (stack, level, entity, seed) -> enumOrdinal(stack, "type", TrapdoorType.FULL));
        ItemProperties.register(blocks.getDoor().asItem(), new ResourceLocation(Constants.DOOR_MODEL_OVERRIDE),
            (stack, level, entity, seed) -> enumOrdinal(stack, "type", DoorType.FULL));
        ItemProperties.register(blocks.getFancyDoor().asItem(), new ResourceLocation(Constants.DOOR_MODEL_OVERRIDE),
            (stack, level, entity, seed) -> enumOrdinal(stack, "type", FancyDoorType.FULL));
        ItemProperties.register(blocks.getFancyTrapdoor().asItem(), new ResourceLocation(Constants.TRAPDOOR_MODEL_OVERRIDE),
            (stack, level, entity, seed) -> enumOrdinal(stack, "type", FancyTrapdoorType.FULL));
        ItemProperties.register(blocks.getPanel().asItem(), new ResourceLocation(Constants.TRAPDOOR_MODEL_OVERRIDE),
            (stack, level, entity, seed) -> enumOrdinal(stack, "type", TrapdoorType.FULL));
        ItemProperties.register(blocks.getPost().asItem(), new ResourceLocation(Constants.POST_MODEL_OVERRIDE),
            (stack, level, entity, seed) -> enumOrdinal(stack, "type", PostType.PLAIN));

        MenuScreens.register(ModContainerTypes.ARCHITECTS_CUTTER.get(), ArchitectsCutterScreen::new);

        final BlockRenderLayerMap layers = BlockRenderLayerMap.INSTANCE;
        final RenderType cutout = RenderType.cutout();
        final RenderType translucent = RenderType.translucent();
        final RenderType solid = RenderType.solid();
        layers.putBlock(blocks.getArchitectsCutter(), cutout);
        layers.putBlock(blocks.getStandingBarrel(), cutout);
        layers.putBlock(blocks.getLayingBarrel(), cutout);
        layers.putBlock(blocks.getShingleSlab(), translucent);
        layers.putBlock(blocks.getPaperWall(), translucent);
        layers.putBlock(blocks.getFence(), translucent);
        layers.putBlock(blocks.getFenceGate(), translucent);
        layers.putBlock(blocks.getSlab(), translucent);
        layers.putBlock(blocks.getStair(), translucent);
        layers.putBlock(blocks.getWall(), translucent);
        layers.putBlock(blocks.getFancyDoor(), translucent);
        layers.putBlock(blocks.getFancyTrapdoor(), translucent);
        layers.putBlock(blocks.getTrapdoor(), translucent);
        layers.putBlock(blocks.getDoor(), translucent);
        layers.putBlock(blocks.getPanel(), translucent);
        layers.putBlock(blocks.getPost(), translucent);

        for (final ShingleHeightType heightType : ShingleHeightType.values()) layers.putBlock(blocks.getShingle(heightType), translucent);
        blocks.getFloatingCarpets().forEach(block -> layers.putBlock(block, cutout));
        blocks.getTimberFrames().forEach(block -> layers.putBlock(block, translucent));
        blocks.getAllBrickBlocks().forEach(block -> layers.putBlock(block, solid));
        blocks.getExtraTopBlocks().forEach(block -> layers.putBlock(block, block instanceof ExtraBlock extra && extra.getType().isTranslucent() ? translucent : solid));

        ClientTickEventHandler.register();
    }

    private static <E extends Enum<E>> float enumOrdinal(final ItemStack stack, final String key, final E fallback)
    {
        if (!stack.hasTag() || !stack.getTag().contains(key)) return 0.0F;
        try
        {
            return Enum.valueOf(fallback.getDeclaringClass(), stack.getTag().getString(key).toUpperCase()).ordinal();
        }
        catch (RuntimeException ignored)
        {
            return fallback.ordinal();
        }
    }
}
