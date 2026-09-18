package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Blocks;

public class ClientLifecycleSubscriber implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(Loader.INSTANCE);
        ClientEventSubscriber.register();

        // replace cauldron with plains default color (4159204, with slighty more light in HSL += 8%)
        ColorProviderRegistry.BLOCK.register(
            (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : 0x638fe9,
            Blocks.WATER_CAULDRON);
    }
}
