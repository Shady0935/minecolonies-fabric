package com.minecolonies.fabric.event.level;

import com.minecolonies.fabric.event.Event;
import net.minecraft.world.level.Level;

public class LevelEvent extends Event
{
    private final Level level;

    protected LevelEvent(final Level level)
    {
        this.level = level;
    }

    public Level getLevel() { return level; }

    public static class Load extends LevelEvent { public Load(final Level level) { super(level); } }
    public static class Unload extends LevelEvent { public Unload(final Level level) { super(level); } }
}
