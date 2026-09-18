package com.ldtteam.domumornamentum.client.model.utils;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Vanilla/Fabric replacement for Forge's quad transformer. */
public final class ModelSpriteQuadTransformer
{
    private static final int VERTEX_STRIDE = 8;
    private static final int UV_OFFSET = 4;

    private ModelSpriteQuadTransformer()
    {
    }

    /** Returns a copy of {@code source} with UVs remapped to {@code target}. */
    public static BakedQuad retexture(final BakedQuad source, final TextureAtlasSprite target)
    {
        final TextureAtlasSprite sourceSprite = source.getSprite();
        final int[] vertices = source.getVertices().clone();
        final float sourceU0 = sourceSprite.getU0();
        final float sourceV0 = sourceSprite.getV0();
        final float sourceUDelta = sourceSprite.getU1() - sourceU0;
        final float sourceVDelta = sourceSprite.getV1() - sourceV0;

        if (sourceUDelta == 0.0F || sourceVDelta == 0.0F)
        {
            return source;
        }

        for (int vertex = 0; vertex < vertices.length / VERTEX_STRIDE; vertex++)
        {
            final int offset = vertex * VERTEX_STRIDE + UV_OFFSET;
            final float u = Float.intBitsToFloat(vertices[offset]);
            final float v = Float.intBitsToFloat(vertices[offset + 1]);
            vertices[offset] = Float.floatToRawIntBits(target.getU(((u - sourceU0) / sourceUDelta) * 16.0F));
            vertices[offset + 1] = Float.floatToRawIntBits(target.getV(((v - sourceV0) / sourceVDelta) * 16.0F));
        }

        return new BakedQuad(vertices, source.getTintIndex(), source.getDirection(), target, source.isShade());
    }
}
