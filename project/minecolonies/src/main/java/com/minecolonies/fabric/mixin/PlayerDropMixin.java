package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.event.entity.item.ItemTossEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bridges the cancellable Forge item-toss event after vanilla creates the entity. */
@Mixin(Player.class)
public abstract class PlayerDropMixin
{
    @Inject(
      method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
      at = @At("RETURN"),
      cancellable = true)
    private void minecolonies$onDrop(final ItemStack stack, final boolean throwRandomly, final boolean retainOwnership,
      final CallbackInfoReturnable<ItemEntity> callbackInfo)
    {
        final Player player = (Player) (Object) this;
        if (player.level().isClientSide())
        {
            return;
        }

        final ItemEntity item = callbackInfo.getReturnValue();
        if (item != null && MinecraftForge.EVENT_BUS.post(new ItemTossEvent(item, player)))
        {
            item.discard();
            callbackInfo.setReturnValue(null);
        }
    }
}
