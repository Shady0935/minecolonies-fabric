package com.ldtteam.domumornamentum.client.model.baked;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.entity.block.IMateriallyTexturedBlockEntity;
import com.ldtteam.domumornamentum.util.MaterialTextureDataUtil;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

/** Fabric baked-model wrapper for Domum Ornamentum's material texture data. */
public final class MateriallyTexturedBakedModel implements BakedModel, FabricBakedModel
{
    private final BakedModel innerModel;

    public MateriallyTexturedBakedModel(final BakedModel innerModel)
    {
        this.innerModel = innerModel;
    }

    @Override
    public List<BakedQuad> getQuads(final BlockState state, final Direction side, final RandomSource random)
    {
        return innerModel.getQuads(state, side, random);
    }

    /**
     * Fabric's renderer passes the item stack here, so item material data can be applied without
     * Forge's ItemOverrides/ModelData extension.
     */
    @Override
    public void emitItemQuads(final ItemStack stack, final Supplier<RandomSource> randomSupplier, final RenderContext context)
    {
        MaterialTextureData textureData = MaterialTextureData.deserializeFromNBT(
            stack.hasTag() ? stack.getOrCreateTagElement("textureData") : new net.minecraft.nbt.CompoundTag());
        if (textureData.isEmpty())
        {
            textureData = MaterialTextureDataUtil.generateRandomTextureDataFrom(stack);
        }

        if (textureData.isEmpty()) context.fallbackConsumer().accept(innerModel);
        else context.bakedModelConsumer().accept(RetexturedBakedModelBuilder.build(innerModel, textureData));
    }

    /** Reads the block entity directly from the render view, replacing Forge ModelData. */
    @Override
    public void emitBlockQuads(final BlockAndTintGetter level,
                               final BlockState state,
                               final BlockPos pos,
                               final Supplier<RandomSource> randomSupplier,
                               final RenderContext context)
    {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IMateriallyTexturedBlockEntity textured && !textured.getTextureData().isEmpty())
        {
            context.bakedModelConsumer().accept(RetexturedBakedModelBuilder.build(innerModel, textured.getTextureData()), state);
        }
        else
        {
            context.fallbackConsumer().accept(innerModel);
        }
    }

    @Override public boolean isVanillaAdapter() { return false; }
    @Override public boolean useAmbientOcclusion() { return innerModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return innerModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return innerModel.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return innerModel.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return innerModel.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return innerModel.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return innerModel.getOverrides(); }
}
