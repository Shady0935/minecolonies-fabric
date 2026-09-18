package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.event.level.ExplosionEvent;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Bridges Forge's explosion-start cancellation at vanilla's explosion phase. */
@Mixin(Explosion.class)
public abstract class ExplosionMixin
{
    @Shadow
    @Final
    private Level level;

    @Unique
    private boolean minecolonies$cancelled;

    @Inject(method = "explode()V", at = @At("HEAD"), cancellable = true)
    private void minecolonies$onStart(final CallbackInfo callbackInfo)
    {
        final Explosion explosion = (Explosion) (Object) this;
        if (MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Start(level, explosion)))
        {
            minecolonies$cancelled = true;
            callbackInfo.cancel();
        }
    }

    @Inject(method = "finalizeExplosion(Z)V", at = @At("HEAD"), cancellable = true)
    private void minecolonies$skipCancelled(final boolean spawnParticles, final CallbackInfo callbackInfo)
    {
        if (minecolonies$cancelled)
        {
            callbackInfo.cancel();
            return;
        }

        final Explosion explosion = (Explosion) (Object) this;
        MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Detonate(level, explosion, explosion.getToBlow(), List.of()));
    }
}
