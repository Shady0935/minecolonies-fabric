package com.minecolonies.coremod.loot;

import com.minecolonies.coremod.MineColonies;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Helper class for supply camp loot
 */
public final class SupplyLoot
{
    /**
     * Resource locations, path and names must fit the existing json file.
     */
    public final static ResourceLocation SUPPLY_CAMP_LT = new ResourceLocation(MOD_ID, "chests/supplycamp");
    public final static ResourceLocation SUPPLY_SHIP_LT = new ResourceLocation(MOD_ID, "chests/supplyship");

    private SupplyLoot()
    {
    }

    private static final Set<ResourceLocation> SUPPLY_CAMP_TARGETS = Set.of(
        new ResourceLocation("minecraft", "chests/spawn_bonus_chest"),
        new ResourceLocation("minecraft", "chests/simple_dungeon"),
        new ResourceLocation("minecraft", "chests/village/village_cartographer"),
        new ResourceLocation("minecraft", "chests/village/village_mason"),
        new ResourceLocation("minecraft", "chests/village/village_desert_house"),
        new ResourceLocation("minecraft", "chests/abandoned_mineshaft"),
        new ResourceLocation("minecraft", "chests/stronghold_library"),
        new ResourceLocation("minecraft", "chests/stronghold_crossing"),
        new ResourceLocation("minecraft", "chests/stronghold_corridor"),
        new ResourceLocation("minecraft", "chests/desert_pyramid"),
        new ResourceLocation("minecraft", "chests/jungle_temple"),
        new ResourceLocation("minecraft", "chests/igloo_chest"),
        new ResourceLocation("minecraft", "chests/woodland_mansion"),
        new ResourceLocation("minecraft", "chests/pillager_outpost"));

    private static final Set<ResourceLocation> SUPPLY_SHIP_TARGETS = Set.of(
        new ResourceLocation("minecraft", "chests/underwater_ruin_small"),
        new ResourceLocation("minecraft", "chests/underwater_ruin_big"),
        new ResourceLocation("minecraft", "chests/buried_treasure"),
        new ResourceLocation("minecraft", "chests/shipwreck_map"),
        new ResourceLocation("minecraft", "chests/shipwreck_supply"),
        new ResourceLocation("minecraft", "chests/shipwreck_treasure"),
        new ResourceLocation("minecraft", "chests/village/village_fisher"),
        new ResourceLocation("minecraft", "chests/village/village_armorer"),
        new ResourceLocation("minecraft", "chests/village/village_temple"));

    /**
     * Fabric equivalent of the Forge global loot modifiers used upstream.
     * The same target-table sets are used, without introducing a custom
     * global-loot-modifier registry.
     */
    public static void register()
    {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, table, source) ->
        {
            if (!MineColonies.getConfig().getCommon().generateSupplyLoot.get())
            {
                return;
            }

            if (SUPPLY_CAMP_TARGETS.contains(id))
            {
                table.withPool(LootPool.lootPool().add(LootTableReference.lootTableReference(SUPPLY_CAMP_LT)));
            }

            if (SUPPLY_SHIP_TARGETS.contains(id))
            {
                table.withPool(LootPool.lootPool().add(LootTableReference.lootTableReference(SUPPLY_SHIP_LT)));
            }
        });
    }
}
