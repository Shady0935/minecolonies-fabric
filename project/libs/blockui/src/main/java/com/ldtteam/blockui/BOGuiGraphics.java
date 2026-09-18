package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.blockui.util.SingleBlockGetter.SingleBlockNeighborhood;
import com.ldtteam.blockui.util.cursor.Cursor;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class BOGuiGraphics
{
    // Static instance should be fine since gui rendering is on single thread
    private static final SingleBlockNeighborhood NEIGHBORHOOD = new SingleBlockNeighborhood();

    private int cursorMaxDepth = -1;
    private Cursor selectedCursor = Cursor.DEFAULT;
    private final Minecraft minecraft;
    private final PoseStack pose;
    private final BufferSource bufferSource;

    public BOGuiGraphics(final Minecraft mc, final PoseStack ps, final BufferSource buffers)
    {
        minecraft = mc;
        pose = ps;
        bufferSource = buffers;
    }

    public PoseStack pose()
    {
        return pose;
    }

    public BufferSource bufferSource()
    {
        return bufferSource;
    }

    public void flush()
    {
        bufferSource.endBatch();
    }

    private Font getFont(@Nullable final ItemStack itemStack)
    {
        if (itemStack != null)
        {
            // Fabric 1.20.1 has no Forge item-font extension hook; vanilla's item count font is the
            // compatible fallback and preserves the normal rendering path.
        }
        return minecraft.font;
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y)
    {
        renderItemDecorations(itemStack, x, y, null);
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize)
    {
        final String stackSize = altStackSize == null ? (itemStack.getCount() == 1 ? null : Integer.toString(itemStack.getCount())) : altStackSize;
        if (stackSize != null)
        {
            final Font font = getFont(itemStack);
            font.drawInBatch(stackSize,
                x + 19 - font.width(stackSize),
                y + 6,
                0xFFFFFF,
                true,
                pose.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                LightTexture.FULL_BRIGHT);
        }

        if (itemStack.isDamaged())
        {
            final int barWidth = Math.round(13.0F - itemStack.getDamageValue() * 13.0F / itemStack.getMaxDamage());
            final int barColor = 255 - Mth.clamp(Math.round(itemStack.getDamageValue() * 255.0F / itemStack.getMaxDamage()), 0, 255);
            UiRenderMacros.fill(pose, x + 2, y + 13, 13, 2, 0xFF000000);
            UiRenderMacros.fill(pose, x + 2, y + 13, barWidth, 1, 0xFF000000 | (barColor << 16) | ((255 - barColor) << 8));
        }
    }

    public int drawString(final String text, final float x, final float y, final int color)
    {
        return drawString(text, x, y, color, false);
    }

    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow)
    {
        return minecraft.font.drawInBatch(text, (int) x, (int) y, color, shadow, pose.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0,
            LightTexture.FULL_BRIGHT);
    }

    public void setCursor(final Cursor cursor)
    {
        if (cursorMaxDepth < 0)
        {
            cursorMaxDepth = 0;
            selectedCursor = cursor;
        }
    }

    /**
     * @param debugXoffset debug string x offset
     */
    public void applyCursor(final int debugXoffset)
    {
        selectedCursor.apply();

        if (Pane.debugging)
        {
            drawString(selectedCursor.toString(), debugXoffset, -minecraft.font.lineHeight, Color.getByName("white"));
        }
    }

    public void renderItem(final ItemStack itemStack, final int x, final int y)
    {
        minecraft.getItemRenderer().renderStatic(itemStack,
            ItemDisplayContext.GUI,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY,
            pose,
            bufferSource,
            minecraft.level,
            0);
    }

    /**
     * Render given blockState with model just like {@link #renderItem(ItemStack, int, int)}
     *
     * @param data      blockState rendering data
     * @param itemStack backing itemStack for given blockState
     */
    public void renderBlockStateAsItem(final BlockStateRenderingData data, final ItemStack itemStack)
    {
        BakedModel itemModel = minecraft.getItemRenderer().getModel(itemStack, null, null, 0);
        if (!itemModel.isGui3d() || data.blockState().getRenderShape() == RenderShape.INVISIBLE)
        {
            // well, some items are bit dumb
            itemModel = minecraft.getItemRenderer().getModel(new ItemStack(Blocks.STONE), null, null, 0);
        }

        // prepare pose just like itemStack rendering would do

        pose().pushPose();
        pose().translate(8, 8, 150);
        pose().mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
        pose().scale(16.0F, 16.0F, 16.0F);
        itemModel.getTransforms().getTransform(ItemDisplayContext.GUI).apply(false, pose());

        if (data.modelNeedsRotationFix())
        {
            pose().pushPose();
            pose().rotateAround(Axis.YP.rotationDegrees(45), 0.0f, 0.5f, 0.0f);
        }

        pose().translate(-0.5F, -0.5F, -0.5F);

        RenderSystem.getModelViewStack().pushPose();
        applyPoseToShader();

        if (data.modelNeedsRotationFix())
        {
            Lighting.setupLevel(new Matrix4f().rotationAround(Axis.ZP.rotationDegrees(-180), 0.5f, 0.0f, 0.5f));
        }
        else
        {
            Lighting.setupLevel(new Matrix4f().rotationAround(Axis.YP.rotationDegrees(-45), 0.0f, 0.5f, 0.0f));
        }

        // render block and BE

        final PoseStack poseStack = new PoseStack();
        final int light = LightTexture.pack(10, 10);
        minecraft.getBlockRenderer()
            .renderSingleBlock(data.blockState(), poseStack, bufferSource(), light, OverlayTexture.NO_OVERLAY);
        if (data.blockEntity() != null)
        {
            try
            {
                minecraft.getBlockEntityRenderDispatcher()
                    .getRenderer(data.blockEntity())
                    .render(data.blockEntity(), 0, poseStack, bufferSource(), light, OverlayTexture.NO_OVERLAY);
            }
            catch (final Exception e)
            {
                // well, noop then
            }
        }
        flush();

        if (data.modelNeedsRotationFix()) // this might need shift before BER?
        {
            pose().popPose();
            pose().translate(-0.5F, -0.5F, -0.5F);
            applyPoseToShader();
        }

        // render fluid

        final FluidState fluidState = data.blockState().getFluidState();
        if (!fluidState.isEmpty())
        {
            final var renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

            NEIGHBORHOOD.blockState = data.blockState();
            minecraft.getBlockRenderer()
                .renderLiquid(BlockPos.ZERO, NEIGHBORHOOD, bufferSource().getBuffer(renderType), data.blockState(), fluidState);

            bufferSource().endBatch(renderType);
        }

        Lighting.setupFor3DItems();

        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        pose().popPose();
    }

    public void applyPoseToShader()
    {
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.getModelViewStack().mulPoseMatrix(pose().last().pose());
        RenderSystem.applyModelViewMatrix();
    }
}
