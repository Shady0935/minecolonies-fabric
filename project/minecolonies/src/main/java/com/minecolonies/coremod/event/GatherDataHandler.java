package com.minecolonies.coremod.event;

/**
 * Datagen bridge kept out of the normal runtime lifecycle while the upstream
 * provider graph is converted to Fabric's FabricDataGenerator API.
 */
public final class GatherDataHandler
{
    private GatherDataHandler() { }

    /**
     * Broad listener signature retained for source compatibility. Fabric
     * datagen will call converted providers from its own entrypoint.
     */
    public static void dataGeneratorSetup(final Object ignoredEvent)
    {
        // Intentionally empty during normal gameplay.
    }
}
