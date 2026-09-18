package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class DefaultEntityTypeTagsProvider extends FabricTagProvider.EntityTypeTagProvider
{
    public DefaultEntityTypeTagsProvider(final FabricDataOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(final HolderLookup.Provider holder)
    {
        getOrCreateTagBuilder(ModTags.hostile).add(EntityType.SLIME);
        getOrCreateTagBuilder(ModTags.mobAttackBlacklist).add(EntityType.ENDERMAN, EntityType.LLAMA);

        final var raiderTagAppender = getOrCreateTagBuilder(ModTags.raiders);
        ModEntities.getRaiders().forEach(raiderType -> raiderTagAppender.add(EntityType.getKey(raiderType)));
    }
}
