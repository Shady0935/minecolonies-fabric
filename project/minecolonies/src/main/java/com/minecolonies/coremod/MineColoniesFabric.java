package com.minecolonies.coremod;

import com.minecolonies.api.crafting.CountedIngredient;
import com.minecolonies.apiimp.initializer.EntityInitializer;
import com.minecolonies.apiimp.initializer.ModBlocksInitializer;
import com.minecolonies.apiimp.initializer.ModItemsInitializer;
import com.minecolonies.apiimp.initializer.ModParticleTypesInitializer;
import com.minecolonies.coremod.recipes.FoodIngredient;
import com.minecolonies.coremod.recipes.PlantIngredient;
import com.minecolonies.coremod.Network;
import com.minecolonies.fabric.capability.RegisterCapabilitiesEvent;
import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.compat.FabricVanillaCompat;
import com.minecolonies.fabric.common.crafting.CraftingHelper;
import com.minecolonies.fabric.event.entity.EntityAttributeCreationEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import com.minecolonies.fabric.registry.FabricRegistries;

/**
 * Fabric bootstrap for the common MineColonies runtime.
 *
 * <p>The source tree still contains a small Forge-shaped event compatibility
 * layer.  This entrypoint gives that layer an explicit, deterministic Fabric
 * lifecycle: vanilla registries are populated first, then the retained common
 * setup and post-init events are dispatched.</p>
 */
public final class MineColoniesFabric implements ModInitializer
{
    private static boolean initialized;

    @Override
    public void onInitialize()
    {
        if (initialized)
        {
            return;
        }

        registerVanillaRegistryListeners();
        postRegister(FabricRegistries.Keys.BLOCKS, FabricRegistries.BLOCKS);
        postRegister(FabricRegistries.Keys.ENTITY_TYPES, FabricRegistries.ENTITY_TYPES);
        postRegister(FabricRegistries.Keys.ITEMS, FabricRegistries.ITEMS);
        postRegister(FabricRegistries.Keys.PARTICLE_TYPES, FabricRegistries.PARTICLE_TYPES);

        // Deferred values that depend on blocks, entities or items are created
        // by the upstream-shaped constructor only after those values exist.
        new MineColonies();

        // Fabric reloads recipes after mod initialization. Register these
        // serializers explicitly here so generated custom ingredients are
        // available before the first server or client recipe reload; the
        // retained Forge-shaped RegisterEvent remains an idempotent bridge.
        CraftingHelper.register(CountedIngredient.ID, CountedIngredient.SERIALIZER);
        CraftingHelper.register(FoodIngredient.ID, FoodIngredient.SERIALIZER);
        CraftingHelper.register(PlantIngredient.ID, PlantIngredient.SERIALIZER);

        MinecraftForge.EVENT_BUS.post(new NewRegistryEvent());
        postRegister(FabricRegistries.Keys.RECIPE_SERIALIZERS, FabricRegistries.RECIPE_SERIALIZERS);
        MinecraftForge.EVENT_BUS.post(new RegisterCapabilitiesEvent());

        final ModConfigEvent.Loading configLoaded = new ModConfigEvent.Loading(new ModConfig(ModConfig.Type.COMMON));
        MinecraftForge.EVENT_BUS.post(configLoaded);
        MinecraftForge.EVENT_BUS.post(new FMLCommonSetupEvent());
        Network.getNetwork().registerServerReceiver();

        final EntityAttributeCreationEvent attributes = new EntityAttributeCreationEvent();
        MinecraftForge.EVENT_BUS.post(attributes);
        FabricVanillaCompat.registerDefaultAttributes(attributes.getAttributes());

        MinecraftForge.EVENT_BUS.post(new FMLLoadCompleteEvent());
        initialized = true;
    }

    public static boolean isInitialized()
    {
        return initialized;
    }

    private static void registerVanillaRegistryListeners()
    {
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleHooks::setCurrentServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ServerLifecycleHooks.setCurrentServer(null));

        MinecraftForge.EVENT_BUS.register(ModBlocksInitializer.class);
        MinecraftForge.EVENT_BUS.register(EntityInitializer.class);
        MinecraftForge.EVENT_BUS.register(ModItemsInitializer.class);
        MinecraftForge.EVENT_BUS.register(ModParticleTypesInitializer.CommonRegistration.class);
    }

    private static <T> void postRegister(final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>> key,
                                         final com.minecolonies.fabric.registry.FabricRegistry<T> registry)
    {
        MinecraftForge.EVENT_BUS.post(new RegisterEvent(key, registry));
    }
}
