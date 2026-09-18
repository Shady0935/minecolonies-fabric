package com.ldtteam.domumornamentum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Fabric placement-preview renderer. The vanilla item renderer consumes the Fabric baked-model
 * wrapper, so material selections still render correctly. Fabric 1.20 does not expose Forge's
 * arbitrary translucent quad hook; the preview therefore uses the normal item lighting/color.
 */
public final class ModelGhostRenderer
{
    private static final ModelGhostRenderer INSTANCE = new ModelGhostRenderer();

    private ModelGhostRenderer()
    {
    }

    public static ModelGhostRenderer getInstance()
    {
        return INSTANCE;
    }

    public void renderGhost(final PoseStack poseStack,
                            final ItemStack stack,
                            final Vec3 target,
                            final BlockHitResult hit,
                            final boolean ignoreDepth)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        final Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        final MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(target.x - camera.x, target.y - camera.y, target.z - camera.z);
        poseStack.scale(1.001F, 1.001F, 1.001F);
        minecraft.getItemRenderer().renderStatic(
            stack,
            ItemDisplayContext.GROUND,
            15728880,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffers,
            minecraft.level,
            0);
        buffers.endBatch();
        poseStack.popPose();
    }
}
