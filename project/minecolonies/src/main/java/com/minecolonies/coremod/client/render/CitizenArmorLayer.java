package com.minecolonies.coremod.client.render;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.fabric.dist.Dist;
import com.minecolonies.fabric.dist.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import org.jetbrains.annotations.NotNull;

/**
 * Citizen armour layer backed by the vanilla 1.20.1 renderer.
 *
 * <p>Forge exposes armour-model and trim hooks directly on
 * {@code HumanoidArmorLayer}; Fabric keeps those implementation details
 * private. Citizens already expose their equipment through vanilla slots, so
 * delegating to the base layer retains armour, dye, trim and glint rendering
 * without depending on Forge-only accessors.</p>
 */
@OnlyIn(Dist.CLIENT)
public class CitizenArmorLayer<T extends AbstractEntityCitizen, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
  extends HumanoidArmorLayer<T, M, A>
{
    public CitizenArmorLayer(final RenderLayerParent<T, M> parentLayer, final A innerModel, final A outerModel,
                             final ModelManager modelManager)
    {
        super(parentLayer, innerModel, outerModel, modelManager);
    }

    @Override
    public void render(@NotNull final PoseStack poseStack, @NotNull final MultiBufferSource bufferSource, final int light,
                       @NotNull final T citizen, final float limbSwing, final float limbSwingAmount, final float partialTick,
                       final float ageInTicks, final float netHeadYaw, final float headPitch)
    {
        if (citizen.getCitizenDataView() != null && citizen.getCitizenDataView().getInventory() != null)
        {
            super.render(poseStack, bufferSource, light, citizen, limbSwing, limbSwingAmount, partialTick,
              ageInTicks, netHeadYaw, headPitch);
        }
    }
}
