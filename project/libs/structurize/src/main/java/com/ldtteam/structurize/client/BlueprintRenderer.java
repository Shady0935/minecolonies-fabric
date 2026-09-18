package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.BlueprintMissHitResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ldtteam.structurize.api.util.constant.Constants.RENDER_PLACEHOLDERS;

/**
 * Renders a blueprint preview directly through Fabric's world render buffer.
 *
 * <p>The Forge implementation pre-baked chunk vertex buffers and depended on
 * Forge model data. Vanilla 1.20.1 already exposes the same block and block
 * entity dispatchers, so this port keeps the preview semantics while drawing
 * through the current Fabric {@link WorldRenderContext}.</p>
 */
public class BlueprintRenderer implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final BlueprintBlockAccess blockAccess;
    private Map<BlockPos, BlockState> renderStates;
    private List<Entity> entities = List.of();
    private List<BlockEntity> tileEntities = List.of();

    public static BlueprintRenderer buildRendererForBlueprint(final Blueprint blueprint)
    {
        return new BlueprintRenderer(new BlueprintBlockAccess(blueprint));
    }

    private BlueprintRenderer(final BlueprintBlockAccess blockAccess)
    {
        this.blockAccess = blockAccess;
    }

    /**
     * Updates the backing simulated level when a preview with the same shape
     * is replaced by freshly loaded data.
     */
    public void updateBlueprint(final BlueprintPreviewData previewData)
    {
        if (previewData == null || previewData.getBlueprint() == null)
        {
            return;
        }
        if (blockAccess.getBlueprint() != previewData.getBlueprint()
            && blockAccess.getBlueprint().hashCode() == previewData.getBlueprint().hashCode())
        {
            blockAccess.setBlueprint(previewData.getBlueprint());
            renderStates = null;
            previewData.scheduleRefresh();
        }
    }

    private void init(final BlockPos anchorPos)
    {
        final Blueprint blueprint = blockAccess.getBlueprint();
        final Minecraft mc = Minecraft.getInstance();
        final BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        final Level serverLevel = mc.hasSingleplayerServer() && mc.player != null
            ? mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).level()
            : null;
        final Level referenceLevel = serverLevel == null ? mc.level : serverLevel;
        final BlockState defaultFluidState = BlockUtils.getFluidForDimension(referenceLevel);

        renderStates = new HashMap<>();
        entities = BlueprintUtils.instantiateEntities(blueprint, blockAccess);
        final Map<BlockPos, Object> modelData = new HashMap<>();
        tileEntities = new ArrayList<>(BlueprintUtils.instantiateTileEntities(blueprint, blockAccess, modelData));

        for (final BlockInfo blockInfo : blueprint.getBlockInfoAsList())
        {
            try
            {
                final BlockPos blockPos = blockInfo.getPos();
                BlockState state = blockInfo.getState();

                if (!BlueprintRenderSettings.instance.renderSettings.get(RENDER_PLACEHOLDERS))
                {
                    if (state.getBlock() == ModBlocks.blockSubstitution.get())
                    {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    else if (state.getBlock() == ModBlocks.blockTagSubstitution.get())
                    {
                        final Optional<BlockEntity> tagEntity = tileEntities.stream()
                            .filter(entity -> entity.getBlockPos().equals(blockPos)
                                && entity instanceof BlockEntityTagSubstitution)
                            .findFirst();
                        if (tagEntity.isPresent())
                        {
                            final BlockEntityTagSubstitution.ReplacementBlock replacement =
                                ((BlockEntityTagSubstitution) tagEntity.get()).getReplacement();
                            state = replacement.getBlockState();
                            tileEntities = new ArrayList<>(tileEntities);
                            tileEntities.remove(tagEntity.get());
                            final BlockEntity replacementEntity = replacement.createBlockEntity(blockPos);
                            if (replacementEntity != null)
                            {
                                replacementEntity.setLevel(blockAccess);
                                tileEntities.add(replacementEntity);
                            }
                        }
                        else
                        {
                            state = Blocks.AIR.defaultBlockState();
                        }
                    }

                    if (state.getBlock() == ModBlocks.blockFluidSubstitution.get())
                    {
                        state = defaultFluidState;
                    }
                    if (SharedConstants.IS_RUNNING_IN_IDE && serverLevel != null
                        && state.getBlock() == ModBlocks.blockSolidSubstitution.get())
                    {
                        final BlockState worldgenState = BlockUtils.getWorldgenBlock(serverLevel,
                            anchorPos.offset(blockPos),
                            blueprint.getRawBlockStateFunction().compose(value -> value.subtract(anchorPos)));
                        if (worldgenState != null)
                        {
                            state = worldgenState;
                        }
                    }
                }

                renderStates.put(blockPos, state);
            }
            catch (final ReportedException exception)
            {
                LOGGER.error("Error while preparing structure part for rendering", exception);
            }
        }
    }

    /**
     * Draws the preview using the active Fabric world render context.
     */
    public void draw(final BlueprintPreviewData previewData, final BlockPos pos, final WorldRenderContext context)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || blockAccess.getBlueprint() == null)
        {
            return;
        }

        updateBlueprint(previewData);
        final Blueprint blueprint = blockAccess.getBlueprint();
        final BlockPos anchorPos = pos.subtract(blueprint.getPrimaryBlockOffset());
        blockAccess.setWorldPos(anchorPos);

        final AABB blueprintBounds = new AABB(BlockPos.ZERO)
            .expandTowards(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1)
            .move(anchorPos);
        if (!context.frustum().isVisible(blueprintBounds))
        {
            return;
        }

        if (previewData.shouldRefresh() || renderStates == null)
        {
            init(anchorPos);
        }

        final PoseStack poseStack = context.matrixStack();
        final MultiBufferSource buffers = context.consumers();
        final BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        poseStack.pushPose();
        poseStack.translate(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ());
        for (final Map.Entry<BlockPos, BlockState> entry : renderStates.entrySet())
        {
            final BlockState state = entry.getValue();
            if (state.getRenderShape() == RenderShape.INVISIBLE
                || state.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED)
            {
                continue;
            }

            poseStack.pushPose();
            final BlockPos blockPos = entry.getKey();
            poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            blockRenderer.renderSingleBlock(state, poseStack, buffers,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        mc.getEntityRenderDispatcher().prepare(blockAccess, context.camera(), mc.crosshairPickEntity);
        for (final Entity entity : entities)
        {
            if (!context.frustum().isVisible(entity.getBoundingBox().move(anchorPos)))
            {
                continue;
            }
            try
            {
                mc.getEntityRenderDispatcher().render(entity,
                    entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), context.tickDelta(),
                    poseStack, buffers, LightTexture.pack(15, 15));
            }
            catch (final ClassCastException exception)
            {
                LOGGER.debug("Skipping entity that cannot be preview-rendered", exception);
            }
        }

        mc.getBlockEntityRenderDispatcher().prepare(blockAccess, context.camera(), BlueprintMissHitResult.MISS);
        for (final BlockEntity tileEntity : tileEntities)
        {
            if (!context.frustum().isVisible(new AABB(tileEntity.getBlockPos()).move(anchorPos)))
            {
                continue;
            }
            final BlockPos blockPos = tileEntity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            mc.getBlockEntityRenderDispatcher().render(tileEntity, context.tickDelta(), poseStack, buffers);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public void close()
    {
        renderStates = null;
        entities = List.of();
        tileEntities = List.of();
    }
}
