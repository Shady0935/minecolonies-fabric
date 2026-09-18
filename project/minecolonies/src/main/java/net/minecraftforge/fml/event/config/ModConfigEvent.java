package net.minecraftforge.fml.event.config;

import net.minecraftforge.fml.config.ModConfig;

/** Configuration lifecycle bridge. */
public class ModConfigEvent
{
    private final ModConfig config;

    protected ModConfigEvent(final ModConfig config)
    {
        this.config = config;
    }

    public ModConfig getConfig()
    {
        return config;
    }

    public static final class Loading extends ModConfigEvent
    {
        public Loading(final ModConfig config)
        {
            super(config);
        }
    }

    public static final class Reloading extends ModConfigEvent
    {
        public Reloading(final ModConfig config)
        {
            super(config);
        }
    }
}
