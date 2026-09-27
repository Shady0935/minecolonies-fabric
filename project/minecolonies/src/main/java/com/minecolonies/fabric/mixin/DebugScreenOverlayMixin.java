package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.client.event.CustomizeGuiOverlayEvent;
import com.minecolonies.fabric.common.MinecraftForge;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Restores Forge's debug-text customization point for the client debug overlay. */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin
{
    @Inject(method = "getGameInformation()Ljava/util/List;", at = @At("RETURN"))
    private void minecolonies$appendDebugOverlayText(final CallbackInfoReturnable<List<String>> callbackInfo)
    {
        MinecraftForge.EVENT_BUS.post(new CustomizeGuiOverlayEvent.DebugText(callbackInfo.getReturnValue()));
    }
}
