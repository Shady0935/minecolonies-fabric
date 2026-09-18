package com.ldtteam.domumornamentum.client.model.baked;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Compatibility wrapper retained for callers that used the Forge render-type split. Fabric 1.20
 * assigns block layers through BlockRenderLayerMap, so the model itself delegates unchanged.
 */
public final class SpecificRenderTypeBakedModelWrapper implements BakedModel
{
    private final RenderType renderType;
    private final BakedModel innerModel;

    public SpecificRenderTypeBakedModelWrapper(final RenderType renderType, final BakedModel innerModel)
    {
        this.renderType = renderType;
        this.innerModel = innerModel;
    }

    public RenderType getRenderType()
    {
        return renderType;
    }

    @Override public List<BakedQuad> getQuads(final BlockState state, final Direction side, final RandomSource random) { return innerModel.getQuads(state, side, random); }
    @Override public boolean useAmbientOcclusion() { return innerModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return innerModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return innerModel.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return innerModel.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return innerModel.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return innerModel.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return innerModel.getOverrides(); }
}
