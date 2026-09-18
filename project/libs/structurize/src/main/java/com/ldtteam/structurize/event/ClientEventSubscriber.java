package com.ldtteam.structurize.event;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.IScrollableItem;
import com.ldtteam.structurize.api.util.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.network.messages.ItemMiddleMouseMessage;
import com.ldtteam.structurize.network.messages.ScanToolTeleportMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Fabric callbacks for Structurize's client-side input and world previews.
 */
public final class ClientEventSubscriber
{
    private static boolean registered;

    private ClientEventSubscriber()
    {
    }

    public static void register()
    {
        if (registered)
        {
            return;
        }
        registered = true;
        ModKeyMappings.register();
        ClientTickEvents.END_CLIENT_TICK.register(ClientEventSubscriber::onClientTickEvent);
        ClientTickEvents.START_CLIENT_TICK.register(ClientEventSubscriber::onPreClientTickEvent);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ClientEventSubscriber::renderWorld);
    }

    private static void renderWorld(final WorldRenderContext context)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
        {
            return;
        }

        final PoseStack poseStack = context.matrixStack();
        final Vec3 viewPosition = context.camera().getPosition();
        final var lineBuffers = WorldRenderMacros.getBufferSource();
        poseStack.pushPose();
        poseStack.translate(-viewPosition.x(), -viewPosition.y(), -viewPosition.z());

        for (final BlueprintPreviewData previewData : RenderingCache.getBlueprintsToRender())
        {
            final Blueprint blueprint = previewData.getBlueprint();
            if (blueprint == null || previewData.getPos() == null)
            {
                continue;
            }

            mc.getProfiler().push("struct_render");
            final BlockPos pos = previewData.getPos();
            final BlockPos posMinusOffset = pos.subtract(blueprint.getPrimaryBlockOffset());
            BlueprintHandler.getInstance().draw(previewData, pos, context);
            WorldRenderMacros.renderRedGlintLineBox(lineBuffers, poseStack, pos, pos, 0.02f);
            WorldRenderMacros.renderWhiteLineBox(lineBuffers, poseStack, posMinusOffset,
                posMinusOffset.offset(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1), 0.02f);
            mc.getProfiler().pop();
        }

        for (final BoxPreviewData previewData : RenderingCache.getBoxesToRender())
        {
            mc.getProfiler().push("struct_box");
            previewData.getAnchor().ifPresent(anchor ->
                WorldRenderMacros.renderRedGlintLineBox(lineBuffers, poseStack, anchor, anchor, 0.02f));
            WorldRenderMacros.renderWhiteLineBox(lineBuffers, poseStack,
                previewData.getPos1(), previewData.getPos2(), 0.02f);
            mc.getProfiler().pop();
        }

        renderTagTool(mc, poseStack, lineBuffers);
        lineBuffers.endBatch();
        poseStack.popPose();
    }

    private static void renderTagTool(final Minecraft mc,
                                      final PoseStack poseStack,
                                      final net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers)
    {
        final Player player = mc.player;
        if (player == null)
        {
            return;
        }
        final ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() != ModItems.tagTool.get()
            || !itemStack.getOrCreateTag().contains(ItemTagTool.TAG_ANCHOR_POS))
        {
            return;
        }

        final BlockPos tagAnchor = BlockPosUtil.readFromNBT(itemStack.getTag(), ItemTagTool.TAG_ANCHOR_POS);
        final BlockEntity blockEntity = player.level().getBlockEntity(tagAnchor);
        WorldRenderMacros.renderRedGlintLineBox(buffers, poseStack, tagAnchor, tagAnchor, 0.02f);
        if (blockEntity instanceof IBlueprintDataProviderBE provider)
        {
            final Map<BlockPos, List<String>> tagPositions = provider.getWorldTagPosMap();
            for (final Map.Entry<BlockPos, List<String>> entry : tagPositions.entrySet())
            {
                WorldRenderMacros.renderWhiteLineBox(buffers, poseStack, entry.getKey(), entry.getKey(), 0.02f);
                WorldRenderMacros.renderDebugText(entry.getKey(), entry.getValue(), poseStack, true, 3, buffers);
            }
        }
    }

    private static void onClientTickEvent(final Minecraft mc)
    {
        mc.getProfiler().push("structurize");
        if (mc.level != null && mc.level.getGameTime() % (Constants.TICKS_SECOND * 5) == 0)
        {
            BlueprintHandler.getInstance().cleanCache();
        }

        if (ModKeyMappings.TELEPORT.get().consumeClick() && mc.level != null && mc.player != null
            && mc.player.getMainHandItem().getItem() instanceof ItemScanTool tool
            && tool.onTeleport(mc.player, mc.player.getMainHandItem()))
        {
            Network.getNetwork().sendToServer(new ScanToolTeleportMessage());
        }
        mc.getProfiler().pop();
    }

    private static void onPreClientTickEvent(final Minecraft mc)
    {
        if (mc.player == null || mc.screen != null || mc.level == null)
        {
            return;
        }

        if (mc.options.keyPickItem.consumeClick())
        {
            BlockPos pos = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK
                ? ((BlockHitResult) mc.hitResult).getBlockPos() : null;
            if (pos != null && mc.level.getBlockState(pos).isAir())
            {
                pos = null;
            }

            final ItemStack current = mc.player.getInventory().getSelected();
            if (current.getItem() instanceof ISpecialBlockPickItem clickableItem)
            {
                final InteractionResult result = clickableItem.onBlockPick(mc.player, current, pos, Screen.hasControlDown());
                switch (result)
                {
                    case PASS -> KeyMapping.click(mc.options.keyPickItem.getDefaultKey());
                    case FAIL -> { }
                    default -> Network.getNetwork().sendToServer(new ItemMiddleMouseMessage(pos, Screen.hasControlDown()));
                }
            }
            else
            {
                KeyMapping.click(mc.options.keyPickItem.getDefaultKey());
            }
        }
    }
}
