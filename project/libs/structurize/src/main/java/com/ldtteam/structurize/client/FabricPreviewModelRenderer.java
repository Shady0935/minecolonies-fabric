package com.ldtteam.structurize.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.BakedModel;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Renders Fabric-only baked models in blueprint previews, which vanilla's single-block path skips. */
final class FabricPreviewModelRenderer
{
    private FabricPreviewModelRenderer()
    {
    }

    static boolean render(final BlockAndTintGetter level,
                          final BlockState state,
                          final BlockPos pos,
                          final PoseStack poseStack,
                          final MultiBufferSource buffers)
    {
        if (!RendererAccess.INSTANCE.hasRenderer()) return false;
        final BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        final BakedModel model = dispatcher.getBlockModel(state);
        if (model.isVanillaAdapter()) return false;
        final FabricBakedModel fabricModel = model;

        final var renderer = RendererAccess.INSTANCE.getRenderer();
        final var meshBuilder = renderer.meshBuilder();
        final RenderMaterial defaultMaterial = renderer.materialFinder().blendMode(BlendMode.DEFAULT).find();
        final List<RenderContext.QuadTransform> transforms = new ArrayList<>();
        final QuadEmitter transformedEmitter = transformedEmitter(meshBuilder.getEmitter(), transforms);
        final Supplier<RandomSource> random = () -> RandomSource.create(pos.asLong());
        final RenderContext[] contextHolder = new RenderContext[1];

        final RenderContext context = new RenderContext()
        {
            @Override public QuadEmitter getEmitter() { return transformedEmitter; }
            @Override public void pushTransform(final QuadTransform transform) { transforms.add(transform); }
            @Override public void popTransform() { if (!transforms.isEmpty()) transforms.remove(transforms.size() - 1); }
            @Override public boolean hasTransform() { return !transforms.isEmpty(); }
            @Override public BakedModelConsumer bakedModelConsumer()
            {
                return new BakedModelConsumer()
                {
                    @Override public void accept(final BakedModel bakedModel) { emitModel(bakedModel, state); }
                    @Override public void accept(final BakedModel bakedModel, final BlockState bakedState) { emitModel(bakedModel, bakedState); }

                    private void emitModel(final BakedModel bakedModel, final BlockState bakedState)
                    {
                        if (!bakedModel.isVanillaAdapter())
                        {
                            bakedModel.emitBlockQuads(level, bakedState, pos, random, thisContext());
                            return;
                        }
                        for (final Direction face : Direction.values()) emitQuads(bakedModel.getQuads(bakedState, face, random.get()));
                        emitQuads(bakedModel.getQuads(bakedState, null, random.get()));
                    }

                    private RenderContext thisContext() { return contextHolder[0]; }

                    private void emitQuads(final List<BakedQuad> quads)
                    {
                        for (final BakedQuad quad : quads)
                        {
                            meshBuilder.getEmitter().fromVanilla(quad, defaultMaterial, quad.getDirection()).emit();
                        }
                    }
                };
            }
        };
        contextHolder[0] = context;

        fabricModel.emitBlockQuads(level, state, pos, random, context);
        final Mesh mesh = meshBuilder.build();
        final var pose = poseStack.last();
        mesh.forEach(quad -> drawQuad(quad, pose, buffers));
        return true;
    }

    private static QuadEmitter transformedEmitter(final QuadEmitter delegate,
                                                   final List<RenderContext.QuadTransform> transforms)
    {
        final QuadEmitter[] proxyRef = new QuadEmitter[1];
        proxyRef[0] = (QuadEmitter) Proxy.newProxyInstance(QuadEmitter.class.getClassLoader(),
            new Class<?>[] {QuadEmitter.class}, (proxy, method, arguments) -> {
                if (method.getName().equals("emit"))
                {
                    for (final RenderContext.QuadTransform transform : transforms)
                    {
                        if (!transform.transform(delegate)) return proxyRef[0];
                    }
                }
                try
                {
                    final Object result = method.invoke(delegate, arguments);
                    return result == delegate ? proxyRef[0] : result;
                }
                catch (final InvocationTargetException exception)
                {
                    throw exception.getCause();
                }
            });
        return proxyRef[0];
    }

    private static void drawQuad(final QuadView quad,
                                 final PoseStack.Pose pose,
                                 final MultiBufferSource buffers)
    {
        final QuadView rendered = quad;
        final RenderMaterial material = rendered.material();
        final RenderType renderType = material.blendMode().blockRenderLayer;
        final VertexConsumer consumer = buffers.getBuffer(renderType);
        final Vector3f faceNormal = rendered.faceNormal();
        for (int vertex = 0; vertex < 4; vertex++)
        {
            final Vector3f normal = rendered.hasNormal(vertex)
                ? rendered.copyNormal(vertex, new Vector3f()) : faceNormal;
            int packedLight = rendered.lightmap(vertex);
            if (packedLight == 0) packedLight = LightTexture.FULL_BRIGHT;
            consumer.vertex(pose.pose(), rendered.x(vertex), rendered.y(vertex), rendered.z(vertex))
                .color(rendered.color(vertex))
                .uv(rendered.u(vertex), rendered.v(vertex))
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), normal.x, normal.y, normal.z)
                .endVertex();
        }
    }
}
