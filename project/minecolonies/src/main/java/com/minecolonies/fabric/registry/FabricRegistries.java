package com.minecolonies.fabric.registry;

import com.mojang.serialization.Codec;
import com.minecolonies.api.util.constant.Constants;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Vanilla/Fabric registry handles used by the port. */
public final class FabricRegistries
{
    public static final FabricRegistry<Block> BLOCKS = vanilla(BuiltInRegistries.BLOCK);
    public static final FabricRegistry<Item> ITEMS = vanilla(BuiltInRegistries.ITEM);
    public static final FabricRegistry<EntityType<?>> ENTITY_TYPES = vanilla(BuiltInRegistries.ENTITY_TYPE);
    public static final FabricRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = vanilla(BuiltInRegistries.BLOCK_ENTITY_TYPE);
    public static final FabricRegistry<ParticleType<?>> PARTICLE_TYPES = vanilla(BuiltInRegistries.PARTICLE_TYPE);
    public static final FabricRegistry<SoundEvent> SOUND_EVENTS = vanilla(BuiltInRegistries.SOUND_EVENT);
    public static final FabricRegistry<Potion> POTIONS = vanilla(BuiltInRegistries.POTION);
    public static final FabricRegistry<Enchantment> ENCHANTMENTS = vanilla(BuiltInRegistries.ENCHANTMENT);
    public static final FabricRegistry<Attribute> ATTRIBUTES = vanilla(BuiltInRegistries.ATTRIBUTE);
    public static final FabricRegistry<MenuType<?>> MENU_TYPES = vanilla(BuiltInRegistries.MENU);
    public static final FabricRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = vanilla(BuiltInRegistries.RECIPE_SERIALIZER);
    public static final FabricRegistry<PoiType> POI_TYPES = vanilla(BuiltInRegistries.POINT_OF_INTEREST_TYPE);
    public static final FabricRegistry<GameEvent> GAME_EVENTS = vanilla(BuiltInRegistries.GAME_EVENT);

    /** Registry keys used by the remaining event-driven source while it is converted to explicit init calls. */
    public static final class Keys
    {
        public static final ResourceKey<Registry<Block>> BLOCKS = Registries.BLOCK;
        public static final ResourceKey<Registry<Item>> ITEMS = Registries.ITEM;
        public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPES = Registries.ENTITY_TYPE;
        public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPES = Registries.PARTICLE_TYPE;
        public static final ResourceKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZERS = Registries.RECIPE_SERIALIZER;
        public static final ResourceKey<Registry<Object>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = RegistryKeyFactory.key("global_loot_modifier_serializers");

        private Keys()
        {
        }
    }

    private static final Map<ResourceLocation, FabricRegistry<?>> CUSTOM = new ConcurrentHashMap<>();

    private FabricRegistries()
    {
    }

    private static <T> FabricRegistry<T> vanilla(final Registry<T> registry)
    {
        return new FabricRegistry<>(registry, registry.key().location(), null);
    }

    @SuppressWarnings("unchecked")
    public static <T> FabricRegistry<T> custom(final ResourceLocation id, final Class<?> entryType)
    {
        return custom(id, entryType, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> FabricRegistry<T> custom(final ResourceLocation id, final Class<?> entryType, final ResourceLocation defaultKey)
    {
        return (FabricRegistry<T>) CUSTOM.computeIfAbsent(id, key -> {
            final Registry<T> registry = FabricRegistryBuilder.createSimple((Class<T>) entryType, key).buildAndRegister();
            return new FabricRegistry<>(registry, key, defaultKey);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> FabricRegistry<T> byVanillaKey(final ResourceKey<? extends Registry<T>> key)
    {
        final Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
        if (registry == null)
        {
            throw new IllegalArgumentException("Unknown vanilla registry: " + key.location());
        }
        return new FabricRegistry<>(registry, key.location(), null);
    }

    public static ResourceLocation id(final String path)
    {
        return new ResourceLocation(Constants.MOD_ID, path);
    }

    private static final class RegistryKeyFactory
    {
        private static <T> ResourceKey<Registry<T>> key(final String path)
        {
            return ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, path));
        }
    }
}
