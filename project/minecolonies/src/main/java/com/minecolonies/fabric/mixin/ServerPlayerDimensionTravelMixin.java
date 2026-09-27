package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.event.ForgeEventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/** Bridges both vanilla ServerPlayer routes that bypass Entity#changeDimension. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDimensionTravelMixin
{
    @Inject(
      method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;",
      at = @At("HEAD"),
      cancellable = true)
    private void minecolonies$onChangeDimension(final ServerLevel destination,
                                                final CallbackInfoReturnable<Entity> callbackInfo)
    {
        if (ForgeEventFactory.onTravelToDimension((ServerPlayer) (Object) this, destination.dimension()))
        {
            callbackInfo.setReturnValue(null);
        }
    }

    @Inject(
      method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z",
      at = @At("HEAD"),
      cancellable = true)
    private void minecolonies$onCrossDimensionTeleport(final ServerLevel destination,
                                                        final double x,
                                                        final double y,
                                                        final double z,
                                                        final Set<RelativeMovement> relativeMovements,
                                                        final float yaw,
                                                        final float pitch,
                                                        final CallbackInfoReturnable<Boolean> callbackInfo)
    {
        final ServerPlayer player = (ServerPlayer) (Object) this;
        if (destination != player.level()
              && ForgeEventFactory.onTravelToDimension(player, destination.dimension()))
        {
            callbackInfo.setReturnValue(false);
        }
    }
}
