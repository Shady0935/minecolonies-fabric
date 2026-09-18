package com.minecolonies.coremod;

import com.minecolonies.apiimp.initializer.ModContainerInitializers;
import com.minecolonies.apiimp.initializer.ModParticleTypesInitializer;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.fabric.client.network.ClientNetworkHooks;
import com.minecolonies.fabric.client.event.EntityRenderersEvent;
import com.minecolonies.fabric.client.event.RegisterClientReloadListenersEvent;
import com.minecolonies.fabric.client.event.RegisterParticleProvidersEvent;
import com.minecolonies.fabric.client.event.RegisterRecipeBookCategoriesEvent;
import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.coremod.event.ClientRegistryHandler;
import com.minecolonies.coremod.event.TextureReloadListener;
import com.minecolonies.fabric.event.TickEvent;
import com.minecolonies.fabric.client.event.ClientPlayerNetworkEvent;
import com.minecolonies.fabric.event.entity.player.ItemTooltipEvent;
import com.minecolonies.fabric.client.event.RenderLevelStageEvent;
import com.minecolonies.coremod.client.render.SpearItemTileEntityRenderer;
import com.minecolonies.api.util.constant.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/** Client-side Fabric bootstrap for renderers, particles and resource reloads. */
public final class MineColoniesClientFabric implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        if (!MineColoniesFabric.isInitialized())
        {
            return;
        }

        ClientNetworkHooks.register(Network.getNetwork().getRawChannel());

        MinecraftForge.EVENT_BUS.register(ClientRegistryHandler.class);
        MinecraftForge.EVENT_BUS.register(ModContainerInitializers.class);
        MinecraftForge.EVENT_BUS.register(ModParticleTypesInitializer.ClientRegistration.class);
        MinecraftForge.EVENT_BUS.register(TextureReloadListener.class);

        registerModelLayers();
        registerRenderers();
        registerBuiltinItemRenderers();
        registerParticles();
        registerReloadListeners();

        ClientTickEvents.START_CLIENT_TICK.register(client ->
          MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START)));
        ClientTickEvents.END_CLIENT_TICK.register(client ->
          MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
          MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingOut()));
        ItemTooltipCallback.EVENT.register((stack, context, lines) ->
          MinecraftForge.EVENT_BUS.post(new ItemTooltipEvent(net.minecraft.client.Minecraft.getInstance().player, stack, lines)));

        // This retained lifecycle event is only consumed by menu screens.  It
        // is dispatched directly so the common setup handler is not repeated.
        ModContainerInitializers.doClientStuff(new FMLClientSetupEvent());
        MinecraftForge.EVENT_BUS.post(new RegisterRecipeBookCategoriesEvent());

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> MinecraftForge.EVENT_BUS.post(
          new RenderLevelStageEvent(context, RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)));
        WorldRenderEvents.LAST.register(context -> MinecraftForge.EVENT_BUS.post(
          new RenderLevelStageEvent(context, RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS)));
    }

    private static void registerModelLayers()
    {
        final EntityRenderersEvent.RegisterLayerDefinitions event = new EntityRenderersEvent.RegisterLayerDefinitions();
        MinecraftForge.EVENT_BUS.post(event);
        for (Map.Entry<ModelLayerLocation, Supplier<LayerDefinition>> entry : event.getDefinitions().entrySet())
        {
            EntityModelLayerRegistry.registerModelLayer(entry.getKey(), entry.getValue()::get);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerRenderers()
    {
        final EntityRenderersEvent.RegisterRenderers event = new EntityRenderersEvent.RegisterRenderers();
        MinecraftForge.EVENT_BUS.post(event);

        for (Map.Entry<EntityType<?>, net.minecraft.client.renderer.entity.EntityRendererProvider<?>> entry : event.getEntityRenderers().entrySet())
        {
            EntityRendererRegistry.INSTANCE.register((EntityType) entry.getKey(), (net.minecraft.client.renderer.entity.EntityRendererProvider) entry.getValue());
        }
        for (Map.Entry<BlockEntityType<?>, net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<?>> entry : event.getBlockEntityRenderers().entrySet())
        {
            BlockEntityRendererRegistry.INSTANCE.register((BlockEntityType) entry.getKey(), (net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider) entry.getValue());
        }
    }

    private static void registerBuiltinItemRenderers()
    {
        if (ModItems.spear == null)
        {
            return;
        }

        // Minecraft has not finished constructing its EntityModelSet while
        // the client entrypoint is running.  Defer the renderer construction
        // until the built-in renderer is actually asked to draw an item.
        final SpearItemTileEntityRenderer[] renderer = new SpearItemTileEntityRenderer[1];
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.spear,
          (stack, poseStack, buffer, light, overlay) ->
          {
              if (renderer[0] == null)
              {
                  renderer[0] = new SpearItemTileEntityRenderer();
              }
              renderer[0].renderByItem(stack, ItemDisplayContext.GUI, poseStack, buffer, light, overlay);
          });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerParticles()
    {
        final RegisterParticleProvidersEvent event = new RegisterParticleProvidersEvent();
        MinecraftForge.EVENT_BUS.post(event);
        final ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();

        for (Map.Entry<ParticleType<?>, ParticleProvider<?>> entry : event.getProviders().entrySet())
        {
            registry.register((ParticleType) entry.getKey(), (ParticleProvider) entry.getValue());
        }
        for (Map.Entry<ParticleType<?>, Function<SpriteSet, ? extends ParticleProvider<?>>> entry : event.getProviderFactories().entrySet())
        {
            registry.register((ParticleType) entry.getKey(), sprites -> (ParticleProvider) entry.getValue().apply(sprites));
        }
    }

    private static void registerReloadListeners()
    {
        final RegisterClientReloadListenersEvent event = new RegisterClientReloadListenersEvent();
        MinecraftForge.EVENT_BUS.post(event);
        int index = 0;
        for (PreparableReloadListener listener : event.getListeners())
        {
            final int listenerIndex = index++;
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener()
            {
                @Override
                public ResourceLocation getFabricId()
                {
                    return new ResourceLocation(Constants.MOD_ID, "client_reload_" + listenerIndex);
                }

                @Override
                public CompletableFuture<Void> reload(final PreparationBarrier barrier, final ResourceManager manager,
                                                       final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler,
                                                       final Executor backgroundExecutor, final Executor gameExecutor)
                {
                    return listener.reload(barrier, manager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
                }
            });
        }
    }
}
