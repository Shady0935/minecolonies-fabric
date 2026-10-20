package com.minecolonies.fabric.config;

import com.minecolonies.api.configuration.Configuration;
import com.ldtteam.structurize.config.fabric.FabricConfigFile;
import com.ldtteam.structurize.config.fabric.ConfigSnapshotCodec;
import com.minecolonies.coremod.MineColonies;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Fabric lifecycle for original client/common/server configuration, including remote authority. */
public final class FabricConfiguration
{
    public static final ResourceLocation CHANNEL = new ResourceLocation("minecolonies", "configuration_v1");
    private static Configuration configuration;
    private static Map<String, Object> localServer, localCommon;
    private static boolean remoteApplied;
    private static Path activeServerFile;
    private FabricConfiguration() { }

    public static void initialize(final Configuration config)
    {
        configuration = config;
        final Path directory = FabricLoader.getInstance().getConfigDir();
        activeServerFile = directory.resolve("minecolonies-server.toml");
        FabricConfigFile.load(config.commonSpec(), directory.resolve("minecolonies-common.toml"));
        FabricConfigFile.load(config.serverSpec(), directory.resolve("minecolonies-server.toml"));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
        { FabricConfigFile.load(config.clientSpec(), directory.resolve("minecolonies-client.toml")); }
        CitizenAIDebugConfig.load();
        CitizenWorkSpeedConfig.load();
        rememberLocal();
    }

    /** An existing Forge-style world override takes precedence; global Fabric configuration remains the default. */
    public static void serverStarting(final MinecraftServer server)
    {
        final Path directory = FabricLoader.getInstance().getConfigDir();
        activeServerFile = directory.resolve("minecolonies-server.toml");
        FabricConfigFile.load(configuration.commonSpec(), directory.resolve("minecolonies-common.toml"));
        FabricConfigFile.load(configuration.serverSpec(), directory.resolve("minecolonies-server.toml"));
        CitizenAIDebugConfig.load();
        CitizenWorkSpeedConfig.load();
        rememberLocal();
        final Path worldConfig = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig/minecolonies-server.toml");
        if (Files.isRegularFile(worldConfig))
        {
            activeServerFile = worldConfig;
            FabricConfigFile.load(configuration.serverSpec(), worldConfig);
            CitizenAIDebugConfig.load(worldConfig);
            CitizenWorkSpeedConfig.load(worldConfig);
        }
    }

    public static Path activeServerFile() { return activeServerFile; }

    public static void sendTo(final ServerPlayer player)
    {
        if (ServerPlayNetworking.canSend(player, CHANNEL))
        {
            final var buffer = PacketByteBufs.create();
            buffer.writeUtf(ConfigSnapshotCodec.encode(configuration.serverSpec(), configuration.commonSpec()), ConfigSnapshotCodec.MAX_LENGTH);
            ServerPlayNetworking.send(player, CHANNEL, buffer);
        }
        else { org.slf4j.LoggerFactory.getLogger("minecolonies/config").warn("Client {} cannot receive server configuration; update both sides.", player.getGameProfile().getName()); }
    }

    public static void applyRemote(final String json)
    {
        ConfigSnapshotCodec.apply(json, configuration.serverSpec(), configuration.commonSpec());
        remoteApplied = true;
        com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer.reconfigureLogging();
    }

    public static void restoreLocal()
    {
        if (remoteApplied)
        {
            configuration.serverSpec().applySnapshot(localServer);
            configuration.commonSpec().applySnapshot(localCommon);
            remoteApplied = false;
            com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer.reconfigureLogging();
        }
    }

    public static void serverStopped()
    {
        configuration.serverSpec().applySnapshot(localServer);
        configuration.commonSpec().applySnapshot(localCommon);
        CitizenAIDebugConfig.load();
        CitizenWorkSpeedConfig.load();
    }

    private static void rememberLocal()
    {
        localServer = configuration.serverSpec().snapshot();
        localCommon = configuration.commonSpec().snapshot();
    }
}
