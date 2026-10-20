package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.client.ClientItemDecorations;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsItemDecorationsMixin
{
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void minecolonies$decorate(final Font font, final ItemStack stack, final int x, final int y,
                                       final String alternateCount, final CallbackInfo callback)
    { ClientItemDecorations.render((GuiGraphics) (Object) this, font, stack, x, y); }
}
