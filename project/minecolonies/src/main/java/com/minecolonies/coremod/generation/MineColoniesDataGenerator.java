package com.minecolonies.coremod.generation;

import com.minecolonies.api.crafting.CountedIngredient;
import com.minecolonies.coremod.recipes.FoodIngredient;
import com.minecolonies.coremod.recipes.PlantIngredient;
import com.minecolonies.coremod.generation.defaults.DefaultBlockLootTableProvider;
import com.minecolonies.coremod.generation.defaults.DefaultBlockTagsProvider;
import com.minecolonies.coremod.generation.defaults.DefaultConventionalBlockTagsProvider;
import com.minecolonies.coremod.generation.defaults.DefaultConventionalItemTagsProvider;
import com.minecolonies.coremod.generation.defaults.DefaultAdvancementsProvider;
import com.minecolonies.coremod.generation.defaults.DefaultDamageTagsProvider;
import com.minecolonies.coremod.generation.defaults.DefaultDamageTypeProvider;
import com.minecolonies.coremod.generation.defaults.DefaultEntityIconProvider;
import com.minecolonies.coremod.generation.defaults.DefaultEntityLootProvider;
import com.minecolonies.coremod.generation.defaults.DefaultEntityTypeTagsProvider;
import com.minecolonies.coremod.generation.defaults.DefaultItemTagsProvider;
import com.minecolonies.coremod.generation.defaults.DefaultRecipeProvider;
import com.minecolonies.coremod.generation.defaults.DefaultResearchProvider;
import com.minecolonies.coremod.generation.defaults.DefaultSupplyLootProvider;
import com.minecolonies.coremod.generation.defaults.QuestTranslationProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultAlchemistCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultBakerCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultBlacksmithCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultConcreteMixerCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultCookAssistantCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultCrusherCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultDyerCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultEnchanterCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultFarmerCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultFishermanLootProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultFletcherCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultGlassblowerCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultLumberjackCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultMechanicCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultNetherWorkerLootProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultPlanterCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultRecipeLootProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultSawmillCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultSifterCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultStonemasonCraftingProvider;
import com.minecolonies.coremod.generation.defaults.workers.DefaultStoneSmelteryCraftingProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import com.minecolonies.fabric.common.crafting.CraftingHelper;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Fabric's replacement for the upstream GatherDataEvent wiring.
 *
 * <p>The providers registered here are the portable 1.20.1 providers. Forge
 * providers that still require an adapter are intentionally left out and are
 * tracked in the porting documentation rather than being silently replaced.</p>
 */
public final class MineColoniesDataGenerator implements DataGeneratorEntrypoint
{
    @Override
    public void buildRegistry(final RegistrySetBuilder builder)
    {
        builder.add(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
          DefaultDamageTypeProvider::bootstrap);
    }

    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator generator)
    {
        CraftingHelper.register(CountedIngredient.ID, CountedIngredient.SERIALIZER);
        CraftingHelper.register(FoodIngredient.ID, FoodIngredient.SERIALIZER);
        CraftingHelper.register(PlantIngredient.ID, PlantIngredient.SERIALIZER);

        final FabricDataGenerator.Pack pack = generator.createPack();
        final DatagenLootTableManager lootTableManager = new DatagenLootTableManager();

        pack.addProvider(provider(DefaultRecipeProvider::new));
        pack.addProvider(provider(DefaultResearchProvider::new));
        pack.addProvider(provider(DefaultAdvancementsProvider::new));
        pack.addProvider(provider(DefaultEntityIconProvider::new));
        pack.addProvider(provider(QuestTranslationProvider::new));
        pack.addProvider(registryProvider(DefaultConventionalBlockTagsProvider::new));
        pack.addProvider(registryProvider(DefaultConventionalItemTagsProvider::new));
        final DefaultBlockTagsProvider blockTags = pack.addProvider(registryProvider(DefaultBlockTagsProvider::new));
        pack.addProvider((output, registries) -> new DefaultItemTagsProvider(output, registries, blockTags));
        pack.addProvider(registryProvider(DefaultEntityTypeTagsProvider::new));
        pack.addProvider(provider(DefaultDamageTypeProvider::new));
        pack.addProvider(registryProvider(DefaultDamageTagsProvider::new));
        pack.addProvider(provider(DefaultBlockLootTableProvider::new));
        pack.addProvider(provider(DefaultEntityLootProvider::new));
        pack.addProvider(provider(DefaultSupplyLootProvider::new));
        pack.addProvider(provider(DefaultFishermanLootProvider::new));
        pack.addProvider(provider(DefaultRecipeLootProvider::new));

        pack.addProvider(provider(output -> new DefaultSifterCraftingProvider(output, lootTableManager)));
        pack.addProvider(provider(output -> new DefaultNetherWorkerLootProvider(output, lootTableManager)));
        pack.addProvider(provider(DefaultAlchemistCraftingProvider::new));
        pack.addProvider(provider(DefaultBakerCraftingProvider::new));
        pack.addProvider(provider(DefaultBlacksmithCraftingProvider::new));
        pack.addProvider(provider(DefaultConcreteMixerCraftingProvider::new));
        pack.addProvider(provider(DefaultCookAssistantCraftingProvider::new));
        pack.addProvider(provider(DefaultCrusherCraftingProvider::new));
        pack.addProvider(provider(DefaultDyerCraftingProvider::new));
        pack.addProvider(provider(DefaultEnchanterCraftingProvider::new));
        pack.addProvider(provider(DefaultFarmerCraftingProvider::new));
        pack.addProvider(provider(DefaultFletcherCraftingProvider::new));
        pack.addProvider(provider(DefaultGlassblowerCraftingProvider::new));
        pack.addProvider(provider(DefaultLumberjackCraftingProvider::new));
        pack.addProvider(provider(DefaultMechanicCraftingProvider::new));
        pack.addProvider(provider(DefaultPlanterCraftingProvider::new));
        pack.addProvider(provider(DefaultSawmillCraftingProvider::new));
        pack.addProvider(provider(DefaultStonemasonCraftingProvider::new));
        pack.addProvider(provider(DefaultStoneSmelteryCraftingProvider::new));

        pack.addProvider(registryProvider(ItemNbtCalculator::new));
    }

    private static <T extends DataProvider> FabricDataGenerator.Pack.Factory<T> provider(
            final Function<FabricDataOutput, T> factory)
    {
        return factory::apply;
    }

    private static <T extends DataProvider> FabricDataGenerator.Pack.RegistryDependentFactory<T> registryProvider(
            final BiFunction<FabricDataOutput, CompletableFuture<HolderLookup.Provider>, T> factory)
    {
        return factory::apply;
    }
}
