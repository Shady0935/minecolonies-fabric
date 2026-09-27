package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.event.ForgeEventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bridges Forge's cancellable pre-travel hook for non-player entities. */
@Mixin(Entity.class)
public abstract class EntityDimensionTravelMixin
{
    @Inject(
      method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;",
      at = @At("HEAD"),
      cancellable = true)
    private void minecolonies$onTravelToDimension(final ServerLevel destination,
                                                   final CallbackInfoReturnable<Entity> callbackInfo)
    {
        if (ForgeEventFactory.onTravelToDimension((Entity) (Object) this, destination.dimension()))
        {
            callbackInfo.setReturnValue(null);
        }
    }
}
