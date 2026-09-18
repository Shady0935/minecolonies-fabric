package net.minecraftforge.fml;

import com.minecolonies.fabric.common.MinecraftForge;

/** Event dispatch facade for the small set of retained upstream calls. */
public final class ModLoader
{
    private static final ModLoader INSTANCE = new ModLoader();

    private ModLoader()
    {
    }

    public static ModLoader get()
    {
        return INSTANCE;
    }

    public void postEvent(final Object event)
    {
        MinecraftForge.EVENT_BUS.post(event);
    }
}
