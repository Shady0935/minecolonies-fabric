package net.minecraftforge.fml.config;

/** Minimal configuration metadata used by the common source. */
public final class ModConfig
{
    public enum Type
    {
        COMMON,
        CLIENT,
        SERVER
    }

    private final Type type;

    public ModConfig(final Type type)
    {
        this.type = type;
    }

    public Type getType()
    {
        return type;
    }
}
