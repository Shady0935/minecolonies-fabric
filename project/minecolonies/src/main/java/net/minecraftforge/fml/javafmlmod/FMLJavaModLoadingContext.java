package net.minecraftforge.fml.javafmlmod;

import com.minecolonies.fabric.event.IEventBus;

/** Registration context bridge retained for source compatibility. */
public final class FMLJavaModLoadingContext
{
    private static final FMLJavaModLoadingContext INSTANCE = new FMLJavaModLoadingContext();

    private FMLJavaModLoadingContext()
    {
    }

    public static FMLJavaModLoadingContext get()
    {
        return INSTANCE;
    }

    public IEventBus getModEventBus()
    {
        return IEventBus.MOD;
    }
}
