package com.minecolonies.fabric.client.event.sound;

import com.minecolonies.fabric.event.Event;
import net.minecraft.client.resources.sounds.SoundInstance;

/** Mutable sound hook used to suppress configured citizen voices. */
public final class PlaySoundEvent extends Event
{
    private SoundInstance sound;

    public PlaySoundEvent(final SoundInstance sound)
    {
        this.sound = sound;
    }

    public SoundInstance getSound()
    {
        return sound;
    }

    public void setSound(final SoundInstance sound)
    {
        this.sound = sound;
    }
}
