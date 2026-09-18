package net.minecraftforge.fml.loading;

import net.fabricmc.loader.api.FabricLoader;

/** Fabric equivalent of Forge's production flag. */
public final class FMLEnvironment
{
    public static final boolean production = !FabricLoader.getInstance().isDevelopmentEnvironment();

    private FMLEnvironment()
    {
    }
}
