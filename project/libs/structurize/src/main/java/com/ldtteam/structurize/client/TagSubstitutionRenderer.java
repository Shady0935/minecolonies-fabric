package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.items.ItemTagSubstitution;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Renders the tag-substitution anchor and the block it currently contains.
 *
 * <p>The Forge renderer used a Forge-only render type and model-data object.
 * Vanilla's 1.20.1 dispatcher already supplies the correct item/model layer,
 * so the Fabric port uses its five-argument {@code renderSingleBlock} path.</p>
 */
public class TagSubstitutionRenderer extends BlockEntityWithoutLevelRenderer implements BlockEntityRenderer<BlockEntityTagSubstitution>
{
    private static TagSubstitutionRenderer INSTANCE;

    public static TagSubstitutionRenderer getInstance()
    {
        return INSTANCE;
    }

    private final BlockEntityRendererProvider.Context context;

    public TagSubstitutionRenderer(@NotNull final BlockEntityRendererProvider.Context context)
    {
        super(context.getBlockEntityRenderDispatcher(), context.getModelSet());
        INSTANCE = this;
        this.context = context;
    }

    @Override
    public void render(@NotNull final BlockEntityTagSubstitution entity,
                       final float partialTick,
                       @NotNull final PoseStack poseStack,
                       @NotNull final MultiBufferSource buffers,
                       final int packedLight,
                       final int packedOverlay)
    {
        render(entity.getReplacement(), entity.getBlockPos(), partialTick, poseStack, buffers, packedLight, packedOverlay);
    }

    @Override
    public void renderByItem(@NotNull final ItemStack stack,
                             @NotNull final ItemDisplayContext transformType,
                             @NotNull final PoseStack poseStack,
                             @NotNull final MultiBufferSource buffers,
                             final int packedLight,
                             final int packedOverlay)
    {
        if (stack.getItem() instanceof ItemTagSubstitution anchor)
        {
            this.context.getBlockRenderDispatcher().renderSingleBlock(anchor.getBlock().defaultBlockState(),
                poseStack, buffers, packedLight, packedOverlay);
            render(anchor.getAbsorbedBlock(stack), BlockPos.ZERO, 0, poseStack, buffers, packedLight, packedOverlay);
        }
    }

    private void render(@NotNull final BlockEntityTagSubstitution.ReplacementBlock replacement,
                        @NotNull final BlockPos pos,
                        final float partialTick,
                        @NotNull final PoseStack poseStack,
                        @NotNull final MultiBufferSource buffers,
                        final int packedLight,
                        final int packedOverlay)
    {
        if (replacement.isEmpty())
        {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(0.98f, 0.98f, 0.98f);
        poseStack.translate(0.01f, 0.01f, 0.01f);

        final BlockRenderDispatcher dispatcher = this.context.getBlockRenderDispatcher();
        final BlockEntity replacementEntity = replacement.getBlockEntity(pos);
        if (replacementEntity != null)
        {
            // A preview block entity still needs a Level for its renderer.
            final Blueprint blueprint = replacement.createBlueprint();
            final BlueprintBlockAccess blockAccess = new BlueprintBlockAccess(blueprint);
            replacementEntity.setLevel(blockAccess);

            if (replacement.getBlockState().getRenderShape() == RenderShape.MODEL)
            {
                dispatcher.renderSingleBlock(replacement.getBlockState(), poseStack, buffers, packedLight, packedOverlay);
            }
            this.context.getBlockEntityRenderDispatcher().render(replacementEntity, partialTick, poseStack, buffers);
        }
        else
        {
            dispatcher.renderSingleBlock(replacement.getBlockState(), poseStack, buffers, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }
}
