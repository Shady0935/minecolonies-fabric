package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.client.event.sound.PlaySoundEvent;
import com.minecolonies.fabric.common.MinecraftForge;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Restores the mutable Forge sound hook at Minecraft's client sound-manager boundary. */
@Mixin(SoundManager.class)
public abstract class SoundManagerMixin
{
    @Redirect(
      method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/sounds/SoundEngine;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
    private void minecolonies$dispatchPlaySoundEvent(final SoundEngine soundEngine, final SoundInstance sound)
    {
        final SoundInstance adjustedSound = minecolonies$adjustSound(sound);
        if (adjustedSound != null)
        {
            soundEngine.play(adjustedSound);
        }
    }

    @Redirect(
      method = "playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/sounds/SoundEngine;playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V"))
    private void minecolonies$dispatchDelayedPlaySoundEvent(final SoundEngine soundEngine,
                                                             final SoundInstance sound,
                                                             final int delay)
    {
        final SoundInstance adjustedSound = minecolonies$adjustSound(sound);
        if (adjustedSound != null)
        {
            soundEngine.playDelayed(adjustedSound, delay);
        }
    }

    @Unique
    private static SoundInstance minecolonies$adjustSound(final SoundInstance sound)
    {
        final PlaySoundEvent event = new PlaySoundEvent(sound);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getSound();
    }
}
