package com.minecolonies.fabric.config;

import com.minecolonies.coremod.MineColonies;
import com.ldtteam.structurize.config.fabric.FabricConfigFile;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

/** Work cadence accessors backed by the same validated, synchronized server tree as original options. */
public final class CitizenWorkSpeedConfig
{
    private CitizenWorkSpeedConfig() { }
    public static double workerAiSpeed() { return MineColonies.getConfig().getServer().workerAiSpeedMultiplier.get(); }
    public static int builderMiningInterval() { return MineColonies.getConfig().getServer().builderMiningCheckInterval.get(); }
    public static boolean builderVerticalReachCompatibility() { return MineColonies.getConfig().getServer().builderVerticalReachCompatibility.get(); }
    public static int miningDelay(final double ticks)
    { return (int) Math.min(Integer.MAX_VALUE, Math.max(0.0, ticks / MineColonies.getConfig().getServer().miningSpeedMultiplier.get())); }
    public static int placementDelay(final int ticks)
    { return ticks <= 0 ? 0 : Math.max(1, (int) Math.ceil(ticks / MineColonies.getConfig().getServer().builderPlacementSpeedMultiplier.get())); }
    public static void load() { load(FabricLoader.getInstance().getConfigDir().resolve("minecolonies-server.toml")); }
    public static void load(final Path file) { FabricConfigFile.load(MineColonies.getConfig().serverSpec(), file); }
}