package com.ldtteam.structurize.config;

import com.ldtteam.structurize.config.fabric.ConfigSnapshotCodec;
import com.ldtteam.structurize.config.fabric.FabricConfigFile;
import com.ldtteam.structurize.config.fabric.FabricConfigSpec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Persist and synchronize Structurize's original fifteen server options. */
public final class ServerConfigurationPersistence
{
    public static final ResourceLocation CHANNEL = new ResourceLocation("structurize", "configuration_v1");
    private static final FabricConfigSpec EMPTY = new FabricConfigSpec.Builder().build();
    private static FabricConfigSpec spec;
    private static Map<String, Object> local;
    private static boolean remoteApplied;
    private ServerConfigurationPersistence() { }

    public static void initialize(final Configuration configuration)
    {
        spec = configuration.serverSpec();
        final Path global = FabricLoader.getInstance().getConfigDir().resolve("structurize-server.toml");
        FabricConfigFile.load(spec, global);
        local = spec.snapshot();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FabricConfigFile.load(spec, global);
            local = spec.snapshot();
            final Path override = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig/structurize-server.toml");
            if (Files.isRegularFile(override)) { FabricConfigFile.load(spec, override); }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.getPlayer(), CHANNEL))
            {
                final var buffer = PacketByteBufs.create();
                buffer.writeUtf(ConfigSnapshotCodec.encode(spec, EMPTY), ConfigSnapshotCodec.MAX_LENGTH);
                ServerPlayNetworking.send(handler.getPlayer(), CHANNEL, buffer);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> spec.applySnapshot(local));
    }

    public static void applyRemote(final String json)
    {
        ConfigSnapshotCodec.apply(json, spec, EMPTY);
        remoteApplied = true;
    }

    public static void restoreLocal()
    {
        if (remoteApplied) { spec.applySnapshot(local); remoteApplied = false; }
    }
}
