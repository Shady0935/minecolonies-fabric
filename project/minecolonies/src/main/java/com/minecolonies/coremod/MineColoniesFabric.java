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
import com.minecolonies.fabric.event.AddReloadListenerEvent;
import com.minecolonies.fabric.event.OnDatapackSyncEvent;
import com.minecolonies.fabric.event.RegisterCommandsEvent;
import com.minecolonies.fabric.event.TagsUpdatedEvent;
import com.minecolonies.fabric.event.TickEvent;
import com.minecolonies.fabric.event.entity.EntityJoinLevelEvent;
import com.minecolonies.fabric.event.entity.player.PlayerEvent;
import com.minecolonies.fabric.event.level.ChunkEvent;
import com.minecolonies.fabric.event.level.LevelEvent;
import com.minecolonies.fabric.event.server.ServerAboutToStartEvent;
import com.minecolonies.fabric.event.server.ServerStartedEvent;
import com.minecolonies.fabric.event.server.ServerStoppingEvent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.minecolonies.fabric.event.entity.EntityAttributeCreationEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import com.minecolonies.fabric.registry.FabricRegistries;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
    private static final Set<net.minecraft.server.MinecraftServer> STARTED_SERVERS =
        Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public void onInitialize()
    {
        if (initialized)
        {
            return;
        }

        registerVanillaRegistryListeners();
        registerFabricLifecycleHooks();
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
        registerDataReloadListeners();

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
        MinecraftForge.EVENT_BUS.register(ModBlocksInitializer.class);
        MinecraftForge.EVENT_BUS.register(EntityInitializer.class);
        MinecraftForge.EVENT_BUS.register(ModItemsInitializer.class);
        MinecraftForge.EVENT_BUS.register(ModParticleTypesInitializer.CommonRegistration.class);
    }

    /**
     * Maps the Fabric lifecycle callbacks that have direct equivalents in the
     * retained Forge-shaped event layer.  Without these bridges the source
     * compiles, but colony ticks, commands, reload listeners and player joins
     * would never reach the upstream handlers.
     */
    private static void registerFabricLifecycleHooks()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            MinecraftForge.EVENT_BUS.post(new RegisterCommandsEvent(dispatcher)));

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) ->
            ServerLifecycleHooks.setCurrentServer(server));
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
        {
            ServerLifecycleHooks.setCurrentServer(server);
            MinecraftForge.EVENT_BUS.post(new ServerAboutToStartEvent(server));
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            ServerLifecycleHooks.setCurrentServer(server);
            STARTED_SERVERS.add(server);
            MinecraftForge.EVENT_BUS.post(new ServerStartedEvent(server));
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
            MinecraftForge.EVENT_BUS.post(new ServerStoppingEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
        {
            STARTED_SERVERS.remove(server);
            ServerLifecycleHooks.setCurrentServer(null);
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) ->
        {
            if (!success)
            {
                return;
            }
            MinecraftForge.EVENT_BUS.post(new TagsUpdatedEvent());
            if (STARTED_SERVERS.contains(server))
            {
                MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent(server, null));
            }
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
            MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent(player.server, player)));

        ServerTickEvents.START_SERVER_TICK.register(server ->
            MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START)));
        ServerTickEvents.END_SERVER_TICK.register(server ->
        {
            MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
            for (final net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers())
            {
                MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(player, TickEvent.Phase.END));
            }
        });
        ServerTickEvents.START_WORLD_TICK.register(level ->
            MinecraftForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(level, TickEvent.Phase.START)));
        ServerTickEvents.END_WORLD_TICK.register(level ->
            MinecraftForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(level, TickEvent.Phase.END)));

        ServerWorldEvents.LOAD.register((server, level) ->
            MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(level)));
        ServerWorldEvents.UNLOAD.register((server, level) ->
            MinecraftForge.EVENT_BUS.post(new LevelEvent.Unload(level)));
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) ->
            MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, level)));
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) ->
            MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk, level)));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) ->
            MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, level)));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(handler.getPlayer())));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(handler.getPlayer())));
    }

    /** Register the portable JSON listeners with Fabric's server data reload pipeline. */
    private static void registerDataReloadListeners()
    {
        final AddReloadListenerEvent event = new AddReloadListenerEvent();
        MinecraftForge.EVENT_BUS.post(event);
        int index = 0;
        for (final PreparableReloadListener listener : event.getListeners())
        {
            final int listenerIndex = index++;
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener()
            {
                @Override
                public ResourceLocation getFabricId()
                {
                    return new ResourceLocation("minecolonies", "data_listener_" + listenerIndex);
                }

                @Override
                public CompletableFuture<Void> reload(final PreparationBarrier barrier,
                                                       final ResourceManager resourceManager,
                                                       final ProfilerFiller preparationsProfiler,
                                                       final ProfilerFiller reloadProfiler,
                                                       final Executor backgroundExecutor,
                                                       final Executor gameExecutor)
                {
                    return listener.reload(barrier, resourceManager, preparationsProfiler, reloadProfiler,
                        backgroundExecutor, gameExecutor);
                }
            });
        }
    }

    private static <T> void postRegister(final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>> key,
                                         final com.minecolonies.fabric.registry.FabricRegistry<T> registry)
    {
        MinecraftForge.EVENT_BUS.post(new RegisterEvent(key, registry));
    }
}
