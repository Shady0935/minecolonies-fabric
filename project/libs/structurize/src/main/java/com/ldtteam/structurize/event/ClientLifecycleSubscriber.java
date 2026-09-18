package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.client.ClientItemStackTooltip;
import com.ldtteam.structurize.client.TagSubstitutionRenderer;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.event.ClientEventSubscriber;
import com.ldtteam.structurize.items.ItemStackTooltip;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import com.ldtteam.structurize.util.BlockUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

/** Fabric client entrypoint replacing Forge's client event bus. */
public final class ClientLifecycleSubscriber implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        Network.getNetwork().registerClientMessages();
        ClientFutureProcessor.register();
        ClientStructurePackLoader.register();
        ClientStructurePackLoader.onClientLoading();
        ClientEventSubscriber.register();
        BlockEntityRendererRegistry.register(ModBlockEntities.TAG_SUBSTITUTION.get(), TagSubstitutionRenderer::new);
        BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.blockTagSubstitution.get().asItem(),
            (stack, poseStack, buffers, packedLight, packedOverlay) -> {
                final TagSubstitutionRenderer renderer = TagSubstitutionRenderer.getInstance();
                if (renderer != null)
                {
                    renderer.renderByItem(stack, ItemDisplayContext.GUI, poseStack, buffers, packedLight, packedOverlay);
                }
            });
        TooltipComponentCallback.EVENT.register(component -> component instanceof ItemStackTooltip tooltip
            ? new ClientItemStackTooltip(tooltip) : null);
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.level != null)
            {
                BlockUtils.checkOrInit();
            }
        });
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.blockSubstitution.get(), RenderType.translucent());
    }
}
