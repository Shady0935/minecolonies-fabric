package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.util.DamageSourceKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;


@SuppressWarnings("unchecked")
public class DefaultDamageTagsProvider extends FabricTagProvider<DamageType>
{
    public DefaultDamageTagsProvider(
      @NotNull final FabricDataOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, Registries.DAMAGE_TYPE, lookupProvider);
    }

    @Override
    protected void addTags(final HolderLookup.Provider lookup)
    {
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR)
          .add(DamageSourceKeys.WAKEY.location(), DamageSourceKeys.GUARD_PVP.location());
        getOrCreateTagBuilder(DamageTypeTags.IS_PROJECTILE)
          .add(DamageSourceKeys.SPEAR.location());
    }

    @Override
    @NotNull
    public String getName()
    {
        return "MineColonies Damage Type Tags";
    }
}
