package com.ldtteam.domumornamentum.client.model.baked;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.client.model.utils.ModelSpriteQuadTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a lightweight model wrapper that replaces placeholder sprites with sprites from the
 * selected material blocks. It keeps the original quad geometry and remaps only the UVs, which
 * is the same observable behavior as the Forge implementation without Forge's ModelData APIs.
 */
public final class RetexturedBakedModelBuilder
{
    private static final RandomSource RANDOM = RandomSource.create();

    private final BakedModel sourceModel;
    private final Map<ResourceLocation, TextureAtlasSprite> replacements = new HashMap<>();
    private final Map<ResourceLocation, Boolean> erased = new HashMap<>();

    private RetexturedBakedModelBuilder(final BakedModel sourceModel)
    {
        this.sourceModel = sourceModel;
    }

    public static RetexturedBakedModelBuilder createFor(final BlockState sourceState,
                                                         final net.minecraft.client.renderer.RenderType renderType,
                                                         final boolean itemStackMode,
                                                         final BakedModel target)
    {
        return new RetexturedBakedModelBuilder(target);
    }

    public static RetexturedBakedModelBuilder createFor(final BlockState sourceState,
                                                         final net.minecraft.client.renderer.RenderType renderType,
                                                         final boolean itemStackMode,
                                                         final BakedModel sourceModel,
                                                         final BakedModel target)
    {
        return new RetexturedBakedModelBuilder(sourceModel);
    }

    public static BakedModel build(final BakedModel source, final MaterialTextureData data)
    {
        final RetexturedBakedModelBuilder builder = new RetexturedBakedModelBuilder(source);
        data.getTexturedComponents().forEach(builder::with);
        return builder.build();
    }

    public RetexturedBakedModelBuilder with(final ResourceLocation source,
                                            final BakedModel target,
                                            final BlockState state)
    {
        final TextureAtlasSprite sprite = firstSprite(target, state);
        if (sprite == null) erased.putIfAbsent(source, true);
        else replacements.putIfAbsent(source, sprite);
        return this;
    }

    public RetexturedBakedModelBuilder with(final ResourceLocation source, final Block target)
    {
        final BlockState state = target.defaultBlockState();
        return with(source, Minecraft.getInstance().getBlockRenderer().getBlockModel(state), state);
    }

    public RetexturedBakedModelBuilder withOut(final ResourceLocation source)
    {
        erased.putIfAbsent(source, true);
        return this;
    }

    public BakedModel build()
    {
        return new RetexturedModel(sourceModel, replacements, erased);
    }

    private static TextureAtlasSprite firstSprite(final BakedModel model, final BlockState state)
    {
        synchronized (RANDOM)
        {
            RANDOM.setSeed(42L);
            for (final Direction direction : Direction.values())
            {
                final List<BakedQuad> quads = model.getQuads(state, direction, RANDOM);
                if (!quads.isEmpty()) return quads.get(0).getSprite();
            }
            final List<BakedQuad> quads = model.getQuads(state, null, RANDOM);
            return quads.isEmpty() ? model.getParticleIcon() : quads.get(0).getSprite();
        }
    }

    private static final class RetexturedModel implements BakedModel
    {
        private final BakedModel delegate;
        private final Map<ResourceLocation, TextureAtlasSprite> replacements;
        private final Map<ResourceLocation, Boolean> erased;

        private RetexturedModel(final BakedModel delegate,
                                final Map<ResourceLocation, TextureAtlasSprite> replacements,
                                final Map<ResourceLocation, Boolean> erased)
        {
            this.delegate = delegate;
            this.replacements = Map.copyOf(replacements);
            this.erased = Map.copyOf(erased);
        }

        @Override
        public List<BakedQuad> getQuads(final BlockState state, final Direction side, final RandomSource random)
        {
            final List<BakedQuad> original = delegate.getQuads(state, side, random);
            if (original.isEmpty()) return original;

            final List<BakedQuad> result = new ArrayList<>(original.size());
            for (final BakedQuad quad : original)
            {
                final ResourceLocation key = quad.getSprite().contents().name();
                if (erased.containsKey(key)) continue;
                final TextureAtlasSprite replacement = replacements.get(key);
                result.add(replacement == null ? quad : ModelSpriteQuadTransformer.retexture(quad, replacement));
            }
            return result;
        }

        @Override public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
        @Override public boolean isGui3d() { return delegate.isGui3d(); }
        @Override public boolean usesBlockLight() { return delegate.usesBlockLight(); }
        @Override public boolean isCustomRenderer() { return delegate.isCustomRenderer(); }
        @Override public TextureAtlasSprite getParticleIcon() { return delegate.getParticleIcon(); }
        @Override public ItemTransforms getTransforms() { return delegate.getTransforms(); }
        @Override public ItemOverrides getOverrides() { return delegate.getOverrides(); }
    }
}
