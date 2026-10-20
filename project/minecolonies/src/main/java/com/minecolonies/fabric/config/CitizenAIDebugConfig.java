package com.minecolonies.fabric.config;

import com.minecolonies.coremod.MineColonies;
import com.ldtteam.structurize.config.fabric.FabricConfigFile;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

/** Compatibility entrypoint: debug keys now belong to the complete server config schema. */
public final class CitizenAIDebugConfig
{
    private CitizenAIDebugConfig() { }
    public static void load() { load(FabricLoader.getInstance().getConfigDir().resolve("minecolonies-server.toml")); }
    public static void load(final Path file) { FabricConfigFile.load(MineColonies.getConfig().serverSpec(), file); }
}