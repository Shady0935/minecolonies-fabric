package com.ldtteam.structurize;

import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.config.Configuration;
import com.ldtteam.structurize.event.EventSubscriber;
import com.ldtteam.structurize.event.LifecycleSubscriber;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.proxy.ClientProxy;
import com.ldtteam.structurize.proxy.IProxy;
import com.ldtteam.structurize.proxy.ServerProxy;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.ServerStructurePackLoader;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import static com.ldtteam.structurize.api.util.constant.Constants.*;

/** Fabric entrypoint for Structurize. */
public class Structurize implements ModInitializer
{
    public static final IProxy proxy = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
      ? new ClientProxy() : new ServerProxy();

    private static Configuration config;

    @Override
    public void onInitialize()
    {
        ModBlocks.initialize();
        ModItems.initialize();
        ModBlockEntities.initialize();
        ModItemGroups.initialize();

        config = new Configuration();

        BlueprintRenderSettings.instance.registerSetting(RENDER_PLACEHOLDERS, false);
        BlueprintRenderSettings.instance.registerSetting(SHARE_PREVIEWS, false);
        BlueprintRenderSettings.instance.registerSetting(DISPLAY_SHARED, false);

        Network.getNetwork().registerCommonMessages();
        EventSubscriber.registerCommon();
        LifecycleSubscriber.registerCommon();
        ServerFutureProcessor.register();
        ServerStructurePackLoader.register();
        ServerPreviewDistributor.register();
        Manager.registerServerLifecycle();
        Log.getLogger().info("Structurize Fabric 1.20.1 initialized");
    }

    public static Configuration getConfig()
    {
        return config;
    }
}
