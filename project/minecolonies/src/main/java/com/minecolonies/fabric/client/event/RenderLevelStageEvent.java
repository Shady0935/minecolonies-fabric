package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.event.Event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

/** Render-stage value object used by MineColonies' world overlays. */
public final class RenderLevelStageEvent extends Event
{
    public enum Stage
    {
        AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS,
        AFTER_TRIPWIRE_BLOCKS
    }

    private final PoseStack poseStack;
    private final float partialTick;
    private final Stage stage;
    private final WorldRenderContext worldRenderContext;

    public RenderLevelStageEvent(final PoseStack poseStack, final float partialTick, final Stage stage)
    {
        this(poseStack, partialTick, stage, null);
    }

    public RenderLevelStageEvent(final WorldRenderContext context, final Stage stage)
    {
        this(context.matrixStack(), context.tickDelta(), stage, context);
    }

    private RenderLevelStageEvent(final PoseStack poseStack, final float partialTick, final Stage stage, final WorldRenderContext context)
    {
        this.poseStack = poseStack;
        this.partialTick = partialTick;
        this.stage = stage;
        this.worldRenderContext = context;
    }

    public PoseStack getPoseStack()
    {
        return poseStack;
    }

    public float getPartialTick()
    {
        return partialTick;
    }

    public Stage getStage()
    {
        return stage;
    }

    public WorldRenderContext getWorldRenderContext()
    {
        return worldRenderContext;
    }
}
