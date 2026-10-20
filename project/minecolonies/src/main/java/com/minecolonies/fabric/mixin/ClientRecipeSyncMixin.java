package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.client.event.RecipesUpdatedEvent;
import com.minecolonies.fabric.common.MinecraftForge;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientRecipeSyncMixin
{
    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void minecolonies$recipesUpdated(final ClientboundUpdateRecipesPacket packet, final CallbackInfo callback)
    { MinecraftForge.EVENT_BUS.post(new RecipesUpdatedEvent(((ClientPacketListener) (Object) this).getRecipeManager())); }
}
