package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.DamageSourceKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/** Fabric datagen for the dynamic MineColonies damage-type registry. */
public class DefaultDamageTypeProvider extends FabricCodecDataProvider<DamageType>
{
    public DefaultDamageTypeProvider(@NotNull final FabricDataOutput output)
    {
        super(output, PackOutput.Target.DATA_PACK, "damage_type", DamageType.CODEC);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "MineColonies Damage Types";
    }

    @Override
    protected void configure(final BiConsumer<ResourceLocation, DamageType> provider)
    {
        getDamageTypes().forEach(provider::accept);
    }

    /** Makes the generated dynamic entries visible to Fabric's tag validation. */
    public static void bootstrap(final BootstapContext<DamageType> context)
    {
        getDamageTypes().forEach((id, type) ->
          context.register(ResourceKey.create(Registries.DAMAGE_TYPE, id), type));
    }

    private static Map<ResourceLocation, DamageType> getDamageTypes()
    {
        return Map.ofEntries(
                Map.entry(DamageSourceKeys.CONSOLE.location(), damage("console")),
                Map.entry(DamageSourceKeys.DEFAULT.location(), damage("default")),
                Map.entry(DamageSourceKeys.DESPAWN.location(), damage("despawn")),
                Map.entry(DamageSourceKeys.NETHER.location(), damage("nether")),

                Map.entry(DamageSourceKeys.GUARD.location(), damage("entity.minecolonies.guard")),
                Map.entry(DamageSourceKeys.GUARD_PVP.location(), damage("entity.minecolonies.guardpvp")),
                Map.entry(DamageSourceKeys.SLAP.location(), damage("entity.minecolonies.slap")),
                Map.entry(DamageSourceKeys.STUCK_DAMAGE.location(), damage("entity.minecolonies.stuckdamage")),
                Map.entry(DamageSourceKeys.TRAINING.location(), damage("entity.minecolonies.training")),
                Map.entry(DamageSourceKeys.WAKEY.location(), damage("entity.minecolonies.wakeywakey")),

                Map.entry(DamageSourceKeys.AMAZON.location(), entityDamage(ModEntities.AMAZON)),
                Map.entry(DamageSourceKeys.AMAZONCHIEF.location(), entityDamage(ModEntities.AMAZONCHIEF)),
                Map.entry(DamageSourceKeys.AMAZONSPEARMAN.location(), entityDamage(ModEntities.AMAZONSPEARMAN)),
                Map.entry(DamageSourceKeys.ARCHERBARBARIAN.location(), entityDamage(ModEntities.ARCHERBARBARIAN)),
                Map.entry(DamageSourceKeys.ARCHERMUMMY.location(), entityDamage(ModEntities.ARCHERMUMMY)),
                Map.entry(DamageSourceKeys.ARCHERPIRATE.location(), entityDamage(ModEntities.ARCHERPIRATE)),
                Map.entry(DamageSourceKeys.BARBARIAN.location(), entityDamage(ModEntities.BARBARIAN)),
                Map.entry(DamageSourceKeys.CHIEFBARBARIAN.location(), entityDamage(ModEntities.CHIEFBARBARIAN)),
                Map.entry(DamageSourceKeys.CHIEFPIRATE.location(), entityDamage(ModEntities.CHIEFPIRATE)),
                Map.entry(DamageSourceKeys.MERCENARY.location(), entityDamage(ModEntities.MERCENARY)),
                Map.entry(DamageSourceKeys.MUMMY.location(), entityDamage(ModEntities.MUMMY)),
                Map.entry(DamageSourceKeys.NORSEMENARCHER.location(), entityDamage(ModEntities.NORSEMEN_ARCHER)),
                Map.entry(DamageSourceKeys.NORSEMENCHIEF.location(), entityDamage(ModEntities.NORSEMEN_CHIEF)),
                Map.entry(DamageSourceKeys.PHARAO.location(), entityDamage(ModEntities.PHARAO)),
                Map.entry(DamageSourceKeys.PIRATE.location(), entityDamage(ModEntities.PIRATE)),
                Map.entry(DamageSourceKeys.SHIELDMAIDEN.location(), entityDamage(ModEntities.SHIELDMAIDEN)),
                Map.entry(DamageSourceKeys.SPEAR.location(), entityDamage(ModEntities.SPEAR)),
                Map.entry(DamageSourceKeys.VISITOR.location(), entityDamage(ModEntities.VISITOR))
        );
    }

    @NotNull
    private static DamageType entityDamage(@NotNull final EntityType<?> entityType)
    {
        return damage(entityType.getDescriptionId());
    }

    @NotNull
    private static DamageType damage(@NotNull final String msgId)
    {
        return new DamageType(msgId, DamageScaling.ALWAYS, 0.1F);
    }
}
