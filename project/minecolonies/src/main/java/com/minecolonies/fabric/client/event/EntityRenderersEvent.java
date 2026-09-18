package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.event.Event;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Client renderer registration records used by the Fabric bootstrap. */
public final class EntityRenderersEvent
{
    private EntityRenderersEvent() { }

    public static final class RegisterLayerDefinitions extends Event
    {
        private final Map<ModelLayerLocation, Supplier<LayerDefinition>> definitions = new LinkedHashMap<>();

        public void registerLayerDefinition(final ModelLayerLocation location, final Supplier<LayerDefinition> definition)
        {
            definitions.put(location, definition);
        }

        public Map<ModelLayerLocation, Supplier<LayerDefinition>> getDefinitions()
        {
            return Map.copyOf(definitions);
        }
    }

    public static final class RegisterRenderers extends Event
    {
        private final Map<EntityType<?>, EntityRendererProvider<?>> entityRenderers = new LinkedHashMap<>();
        private final Map<BlockEntityType<?>, BlockEntityRendererProvider<?>> blockEntityRenderers = new LinkedHashMap<>();

        public <T extends Entity> void registerEntityRenderer(final EntityType<? extends T> type, final EntityRendererProvider<? super T> provider)
        {
            entityRenderers.put(type, provider);
        }

        public <T extends BlockEntity> void registerBlockEntityRenderer(final BlockEntityType<? extends T> type, final BlockEntityRendererProvider<? super T> provider)
        {
            blockEntityRenderers.put(type, provider);
        }

        public Map<EntityType<?>, EntityRendererProvider<?>> getEntityRenderers()
        {
            return Map.copyOf(entityRenderers);
        }

        public Map<BlockEntityType<?>, BlockEntityRendererProvider<?>> getBlockEntityRenderers()
        {
            return Map.copyOf(blockEntityRenderers);
        }
    }
}
