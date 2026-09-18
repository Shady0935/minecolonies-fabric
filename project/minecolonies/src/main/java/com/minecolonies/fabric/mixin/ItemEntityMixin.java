package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.event.entity.player.EntityItemPickupEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bridges the cancellable Forge item-pickup event before vanilla consumes the stack. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin
{
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void minecolonies$onPlayerTouch(final Player player, final CallbackInfo callbackInfo)
    {
        if (player.level().isClientSide())
        {
            return;
        }

        final ItemEntity item = (ItemEntity) (Object) this;
        if (MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(player, item)))
        {
            callbackInfo.cancel();
        }
    }
}
