package net.minecraftforge.fml.event.lifecycle;

/** Fabric-side lifecycle bridge. */
public class FMLCommonSetupEvent
{
    public void enqueueWork(final Runnable work)
    {
        if (work != null) work.run();
    }
}
