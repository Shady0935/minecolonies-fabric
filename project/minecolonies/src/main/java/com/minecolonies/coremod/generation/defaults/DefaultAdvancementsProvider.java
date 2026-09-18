package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.advancements.all_towers.AllTowersCriterionInstance;
import com.minecolonies.api.advancements.army_population.ArmyPopulationCriterionInstance;
import com.minecolonies.api.advancements.building_add_recipe.BuildingAddRecipeCriterionInstance;
import com.minecolonies.api.advancements.citizen_bury.CitizenBuryCriterionInstance;
import com.minecolonies.api.advancements.citizen_eat_food.CitizenEatFoodCriterionInstance;
import com.minecolonies.api.advancements.citizen_resurrect.CitizenResurrectCriterionInstance;
import com.minecolonies.api.advancements.click_gui_button.ClickGuiButtonCriterionInstance;
import com.minecolonies.api.advancements.colony_population.ColonyPopulationCriterionInstance;
import com.minecolonies.api.advancements.complete_build_request.CompleteBuildRequestCriterionInstance;
import com.minecolonies.api.advancements.create_build_request.CreateBuildRequestCriterionInstance;
import com.minecolonies.api.advancements.deep_mine.DeepMineCriterionInstance;
import com.minecolonies.api.advancements.max_fields.MaxFieldsCriterionInstance;
import com.minecolonies.api.advancements.place_structure.PlaceStructureCriterionInstance;
import com.minecolonies.api.advancements.place_supply.PlaceSupplyCriterionInstance;
import com.minecolonies.api.advancements.undertaker_totem.UndertakerTotemCriterionInstance;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.WindowConstants;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for advancements
 */
public class DefaultAdvancementsProvider extends FabricAdvancementProvider
{
    @Override
    public void generateAdvancement(@NotNull final Consumer<Advancement> consumer)
    {
        // todo: the achievement ids are a bit weird, in particular the folder organisation;
        //       at some major MC version update we should probably reorganise them.

        // this is mostly redundant with the standard root, but it lets people see a Minecolonies
        // advancement before that tab is visible...
        Advancement.Builder.advancement()
          .parent(externalParent(new ResourceLocation("story/root")))
          .display(ModItems.supplyChest,
            Component.translatable("advancements.minecolonies.root.title"),
            Component.translatable("advancements.minecolonies.root.description"),
            null,
            FrameType.TASK, false, false, false)
          .addCriterion("supply_ship", new PlaceSupplyCriterionInstance())
          .save(consumer, new ResourceLocation(MOD_ID, "minecraft/craft_supply").toString());

        addStandardAdvancements(consumer);
        addProductionAdvancements(consumer);
        addMilitaryAdvancements(consumer);
    }

    public DefaultAdvancementsProvider(@NotNull final FabricDataOutput output)
    {
        super(output);
    }

    private static void addStandardAdvancements(@NotNull final Consumer<Advancement> consumer)
    {
        final String GROUP = "minecolonies/";

        final Advancement root = Advancement.Builder.advancement()
                .display(ModItems.supplyChest,
                        Component.translatable("advancements.minecolonies.root.title"),
                        Component.translatable("advancements.minecolonies.root.description"),
                        new ResourceLocation("textures/block/light_gray_wool.png"),
                        FrameType.TASK, true, true, false)
                .addCriterion("supply_ship_placed", new PlaceSupplyCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root").toString());

        final Advancement placeTownHall = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutTownHall,"place_townhall"))
                .addCriterion("build_tool_place_town_hall", placeStructure(ModBuildings.townHall.get()))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "place_townhall").toString());

        final Advancement startBuilder = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(FrameType.TASK, ModBlocks.blockConstructionTape,"create_build_request_1_builder"))
                .addCriterion("builder_request", createBuildRequest(ModBuildings.builder.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "start_builder").toString());

        final Advancement fulfillRequest = Advancement.Builder.advancement()
                .parent(startBuilder)
                .display(make(FrameType.TASK, ModItems.resourceScroll,"click_gui_button_fulfill"))
                .addCriterion("click_gui_button_fulfill",
                        new ClickGuiButtonCriterionInstance(WindowConstants.REQUEST_FULLFIL, MOD_ID + WindowConstants.CITIZEN_REQ_RESOURCE_SUFFIX))
                .addCriterion("click_request_button_fulfill",
                        new ClickGuiButtonCriterionInstance(WindowConstants.REQUEST_FULLFIL, MOD_ID + WindowConstants.CITIZEN_REQ_DETAIL_SUFFIX))
                .requirements(RequirementsStrategy.OR)
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "fulfill_request").toString());

        final Advancement buildBuilder = Advancement.Builder.advancement()
                .parent(fulfillRequest)
                .display(make(FrameType.GOAL, ModBlocks.blockHutBuilder,"build.builder"))
                .addCriterion("builders_hut", completeBuildRequest(ModBuildings.builder.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_builder").toString());

        final Advancement buildBuilder2 = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.GOAL, ModBlocks.blockHutBuilder, "complete_build_request_2_builder"))
                .addCriterion("builders_hut", completeBuildRequest(ModBuildings.builder.get(), 2))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_builder_2").toString());

        final Advancement buildGuardTower = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.TASK, ModBlocks.blockHutGuardTower, "build.guard_tower"))
                .addCriterion("guard_tower", completeBuildRequest(ModBuildings.guardTower.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_guard_tower").toString());

        final Advancement buildMysticalSite = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.TASK, ModBlocks.blockHutMysticalSite, "build.mysticalsite"))
                .addCriterion("mysticalsite", completeBuildRequest(ModBuildings.mysticalSite.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_mysticalsite").toString());

        final Advancement buildMysticalSite5 = Advancement.Builder.advancement()
                .parent(buildMysticalSite)
                .display(make(FrameType.CHALLENGE, ModBlocks.blockHutMysticalSite, "build.mysticalsite_5"))
                .addCriterion("mysticalsite", completeBuildRequest(ModBuildings.mysticalSite.get(), 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_mysticalsite_5").toString());

        final Advancement buildTavern = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.TASK, ModBlocks.blockHutTavern, "build.tavern"))
                .addCriterion("tavern", completeBuildRequest(ModBuildings.tavern.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_tavern").toString());

        final Advancement buildCitizen = Advancement.Builder.advancement()
                .parent(buildTavern)
                .display(make(FrameType.TASK, ModBlocks.blockHutHome, "build.citizen"))
                .addCriterion("citizens_hut",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.HOME_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_citizen").toString());

        final Advancement buildTownHall = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.TASK, ModBlocks.blockHutTownHall, "build.town_hall"))
                .addCriterion("town_hall",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.TOWNHALL_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_town_hall").toString());

        final Advancement buildTownHall5 = Advancement.Builder.advancement()
                .parent(buildTownHall)
                .display(make(FrameType.CHALLENGE, ModBlocks.blockHutTownHall, "complete_build_request_5_town_hall"))
                .addCriterion("town_hall", completeBuildRequest(ModBuildings.townHall.get(), 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_town_hall_5").toString());

        final Advancement buildingAddRecipeTorch = Advancement.Builder.advancement()
                .parent(startBuilder)
                .display(make(FrameType.TASK, Items.TORCH, "building_add_recipe_torch"))
                .addCriterion("add_recipe_torch",
                        new BuildingAddRecipeCriterionInstance(item(Items.TORCH), 2))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "building_add_recipe_torch").toString());

        final Advancement checkOutGuide = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(FrameType.TASK, ModBlocks.blockHutBuilder, "check_out_guide"))
                .addCriterion("click_gui_button_close",
                        new ClickGuiButtonCriterionInstance(WindowConstants.GUIDE_CONFIRM, MOD_ID + WindowConstants.GUIDE_RESOURCE_SUFFIX))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "check_out_guide").toString());

        final Advancement citizenEatFood = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(FrameType.TASK, Items.COOKED_BEEF, "citizen_eat_food_first"))
                .addCriterion("citizen_eat_anything", new CitizenEatFoodCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_eat_food_first").toString());

        final Advancement citizenEatFoodRottenFlesh = Advancement.Builder.advancement()
                .parent(citizenEatFood)
                .display(makeHidden(FrameType.TASK, Items.ROTTEN_FLESH, "citizen_eat_food_rotten_flesh"))
                .addCriterion("citizen_eat_rotten_flesh",
                        new CitizenEatFoodCriterionInstance(item(Items.ROTTEN_FLESH)))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_eat_food_rotten_flesh").toString());

        final Advancement colonyPopulation5 = Advancement.Builder.advancement()
                .parent(buildCitizen)
                .display(make(FrameType.GOAL, Items.RED_BED, "colony_population_5"))
                .addCriterion("population_5", new ColonyPopulationCriterionInstance(5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_5").toString());

        final Advancement colonyPopulation10 = Advancement.Builder.advancement()
                .parent(colonyPopulation5)
                .display(make(FrameType.TASK, Items.BRICK, "colony_population_10"))
                .addCriterion("population_10", new ColonyPopulationCriterionInstance(10))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_10").toString());

        final Advancement colonyPopulation25 = Advancement.Builder.advancement()
                .parent(colonyPopulation10)
                .display(make(FrameType.GOAL, Items.GOLD_INGOT, "colony_population_25"))
                .addCriterion("population_25", new ColonyPopulationCriterionInstance(25))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_25").toString());

        final Advancement colonyPopulation50 = Advancement.Builder.advancement()
                .parent(colonyPopulation25)
                .display(make(FrameType.GOAL, Items.DIAMOND, "colony_population_50"))
                .addCriterion("population_50", new ColonyPopulationCriterionInstance(50))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_50").toString());

        final Advancement colonyPopulation75 = Advancement.Builder.advancement()
                .parent(colonyPopulation50)
                .display(make(FrameType.CHALLENGE, Items.EMERALD, "colony_population_75"))
                .addCriterion("population_75", new ColonyPopulationCriterionInstance(75))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_75").toString());

        final Advancement citizenBury = Advancement.Builder.advancement()
                .parent(buildTavern)
                .display(make(FrameType.TASK, ModBlocks.blockHutGraveyard, "citizen.bury"))
                .addCriterion("buried", new CitizenBuryCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_bury").toString());

        final Advancement citizenResurrect = Advancement.Builder.advancement()
                .parent(citizenBury)
                .display(make(FrameType.TASK, ModBlocks.blockHutGraveyard, "citizen.resurrect"))
                .addCriterion("resurrected", new CitizenResurrectCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_resurrect").toString());

        final Advancement undertakerTotem = Advancement.Builder.advancement()
                .parent(citizenResurrect)
                .display(make(FrameType.TASK, Items.TOTEM_OF_UNDYING, "undertaker.totem"))
                .addCriterion("totem", new UndertakerTotemCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "undertaker_totem").toString());
    }

    private static void addProductionAdvancements(@NotNull final Consumer<Advancement> consumer)
    {
        final String GROUP = "production/";

        final Advancement root = Advancement.Builder.advancement()
                .display(ModBlocks.blockHutBuilder,
                        Component.translatable("advancements.minecolonies.root.production.title"),
                        Component.translatable("advancements.minecolonies.root.production.description"),
                        new ResourceLocation("structurize:textures/block/cactus/cactus_planks.png"),
                        FrameType.TASK, false, false, false)
                .addCriterion("builders_hut", completeBuildRequest(ModBuildings.builder.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root").toString());

        // --- food ---

        final Advancement buildCook = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutCook, "build.cook"))
                .addCriterion("cook", completeBuildRequest(ModBuildings.cook.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_cook").toString());

        final Advancement buildBaker = Advancement.Builder.advancement()
                .parent(buildCook)
                .display(make(FrameType.TASK, ModBlocks.blockHutBaker, "build.baker"))
                .addCriterion("bakery", completeBuildRequest(ModBuildings.bakery.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_baker").toString());

        // --- logistics ---

        final Advancement buildWarehouse = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutWareHouse, "build.warehouse"))
                .addCriterion("warehouse", completeBuildRequest(ModBuildings.wareHouse.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_warehouse").toString());

        final Advancement buildDeliveryPerson = Advancement.Builder.advancement()
                .parent(buildWarehouse)
                .display(make(FrameType.TASK, ModBlocks.blockHutDeliveryman, "build.delivery_person"))
                .addCriterion("delivery_person", completeBuildRequest(ModBuildings.deliveryman.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_delivery_person").toString());

        final Advancement postAndStash = Advancement.Builder.advancement()
                .parent(buildDeliveryPerson)
                .display(make(FrameType.TASK, ModBlocks.blockPostBox, "post_and_stash"))
                .addCriterion("postbox", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.blockPostBox))
                .addCriterion("stash", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.blockStash))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "post_and_stash").toString());

        // --- education ---

        final Advancement buildLibrary = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutLibrary, "build.library"))
                .addCriterion("library", completeBuildRequest(ModBuildings.library.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_library").toString());

        final Advancement buildEnchanter = Advancement.Builder.advancement()
                .parent(buildLibrary)
                .display(make(FrameType.TASK, ModBlocks.blockHutEnchanter, "build.enchanter"))
                .addCriterion("enchanter", completeBuildRequest(ModBuildings.enchanter.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_enchanter").toString());

        // --- crafting ---

        final Advancement buildSawmill = Advancement.Builder.advancement()
                .parent(buildDeliveryPerson)
                .display(make(FrameType.TASK, ModBlocks.blockHutSawmill, "build.sawmill"))
                .addCriterion("sawmill", completeBuildRequest(ModBuildings.sawmill.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_sawmill").toString());

        final Advancement buildBlacksmith = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutBlacksmith, "build.blacksmith"))
                .addCriterion("blacksmith", completeBuildRequest(ModBuildings.blacksmith.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_blacksmith").toString());

        final Advancement buildSmeltery = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery"))
                .addCriterion("smeltery", completeBuildRequest(ModBuildings.smeltery.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery").toString());

        final Advancement buildSmeltery3 = Advancement.Builder.advancement()
                .parent(buildSmeltery)
                .display(make(FrameType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery_3"))
                .addCriterion("smeltery", completeBuildRequest(ModBuildings.smeltery.get(), 3))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery_3").toString());

        final Advancement buildSmeltery5 = Advancement.Builder.advancement()
                .parent(buildSmeltery3)
                .display(make(FrameType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery_5"))
                .addCriterion("smeltery", completeBuildRequest(ModBuildings.smeltery.get(), 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery_5").toString());

        final Advancement buildStoneSmeltery = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutStoneSmeltery, "build.stone_smeltery"))
                .addCriterion("stone_smeltery", completeBuildRequest(ModBuildings.stoneSmelter.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_stone_smeltery").toString());

        final Advancement buildStoneMason = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutStonemason, "build.stonemason"))
                .addCriterion("stonemason", completeBuildRequest(ModBuildings.stoneMason.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_stonemason").toString());

        final Advancement buildCrusher = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutCrusher, "build.crusher"))
                .addCriterion("crusher", completeBuildRequest(ModBuildings.crusher.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_crusher").toString());

        final Advancement buildSifter = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutSifter, "build.sifter"))
                .addCriterion("sifter", completeBuildRequest(ModBuildings.sifter.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_sifter").toString());

        final Advancement buildingAddRecipeDoor = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, Items.OAK_DOOR, "building_add_recipe_door"))
                .addCriterion("add_recipe_oak_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.OAK_DOOR), 3))
                .addCriterion("add_recipe_birch_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.BIRCH_DOOR), 3))
                .addCriterion("add_recipe_dark_oak_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.DARK_OAK_DOOR), 3))
                .addCriterion("add_recipe_spruce_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.SPRUCE_DOOR), 3))
                .addCriterion("add_recipe_jungle_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.JUNGLE_DOOR), 3))
                .addCriterion("add_recipe_acacia_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.ACACIA_DOOR), 3))
                .save(consumer, new ResourceLocation(MOD_ID, "minecolonies/building_add_recipe_door").toString());

        // --- farming ---

        final Advancement buildFarmer = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutFarmer, "build.farmer"))
                .addCriterion("farmer", completeBuildRequest(ModBuildings.farmer.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_farmer").toString());

        final Advancement maxFields = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(FrameType.TASK, ModBlocks.blockScarecrow, "max_fields"))
                .addCriterion("fields", new MaxFieldsCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "max_fields").toString());

        final Advancement buildFisherman = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutFisherman, "build.fisherman"))
                .addCriterion("fisherman", completeBuildRequest(ModBuildings.fisherman.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_fisherman").toString());

        final Advancement buildLumberjack = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutLumberjack, "build.lumberjack"))
                .addCriterion("lumberjack", completeBuildRequest(ModBuildings.lumberjack.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_lumberjack").toString());

        final Advancement buildMiner = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutMiner, "build.miner"))
                .addCriterion("miner", completeBuildRequest(ModBuildings.miner.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_miner").toString());

        final Advancement deepMine = Advancement.Builder.advancement()
                .parent(buildMiner)
                .display(make(FrameType.TASK, Items.BEDROCK, "deep_mine"))
                .addCriterion("mineshaft", new DeepMineCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "deep_mine").toString());

        final Advancement buildComposter = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(FrameType.TASK, ModBlocks.blockHutComposter, "build.composter"))
                .addCriterion("composter", completeBuildRequest(ModBuildings.composter.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_composter").toString());

        final Advancement buildFlorist = Advancement.Builder.advancement()
                .parent(buildComposter)
                .display(make(FrameType.TASK, ModBlocks.blockHutFlorist, "build.florist"))
                .addCriterion("florist", completeBuildRequest(ModBuildings.florist.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_florist").toString());

        final Advancement buildAllHerders = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(FrameType.GOAL, ModBlocks.blockHutShepherd, "build.herders"))
                .addCriterion("sheep", completeBuildRequest(ModBuildings.shepherd.get(), 1))
                .addCriterion("cows", completeBuildRequest(ModBuildings.cowboy.get(), 1))
                .addCriterion("chickens", completeBuildRequest(ModBuildings.chickenHerder.get(), 1))
                .addCriterion("pigs", completeBuildRequest(ModBuildings.swineHerder.get(), 1))
                .addCriterion("rabbits", completeBuildRequest(ModBuildings.rabbitHutch.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_all_herders").toString());
    }

    private static void addMilitaryAdvancements(@NotNull final Consumer<Advancement> consumer)
    {
        final String GROUP = "military/";

        final Advancement root = Advancement.Builder.advancement()
                .display(ModBlocks.blockHutBarracks,
                        Component.translatable("advancements.minecolonies.root.military.title"),
                        Component.translatable("advancements.minecolonies.root.military.description"),
                        new ResourceLocation("textures/block/stone_bricks.png"),
                        FrameType.TASK, true, false, false)
                .addCriterion("guardtower", completeBuildRequest(ModBuildings.guardTower.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root").toString());

        final Advancement buildArchery = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutArchery, "build.archery"))
                .addCriterion("archery", completeBuildRequest(ModBuildings.archery.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_archery").toString());

        final Advancement buildCombatAcademy = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutCombatAcademy, "build.combat_academy"))
                .addCriterion("combat_academy", completeBuildRequest(ModBuildings.combatAcademy.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_combat_academy").toString());

        final Advancement buildBarracks = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutBarracks, "build.barracks"))
                .addCriterion("barracks", completeBuildRequest(ModBuildings.barracks.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_barracks").toString());

        final Advancement buildBarracksTower = Advancement.Builder.advancement()
                .parent(buildBarracks)
                .display(make(FrameType.TASK, ModBlocks.blockHutBarracksTower, "build.barracks_tower"))
                .addCriterion("barracks_tower", completeBuildRequest(ModBuildings.barracksTower.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_barracks_tower").toString());

        final Advancement buildAllBarracksTowers = Advancement.Builder.advancement()
                .parent(buildBarracksTower)
                .display(make(FrameType.GOAL, ModBlocks.blockHutBarracksTower, "build.all_towers"))
                .addCriterion("towers", new AllTowersCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_all_barracks_towers").toString());

        final Advancement army8 = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, Items.STONE_SWORD, "army_8"))
                .addCriterion("population_5", new ArmyPopulationCriterionInstance(8))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_8").toString());

        final Advancement army30 = Advancement.Builder.advancement()
                .parent(army8)
                .display(make(FrameType.TASK, Items.IRON_SWORD, "army_30"))
                .addCriterion("population_5", new ArmyPopulationCriterionInstance(30))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_30").toString());

        final Advancement army80 = Advancement.Builder.advancement()
                .parent(army30)
                .display(make(FrameType.CHALLENGE, Items.DIAMOND_SWORD, "army_80"))
                .addCriterion("population_5", new ArmyPopulationCriterionInstance(80))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_80").toString());
    }

    private static DisplayInfo make(@NotNull final FrameType frame,
                                    @NotNull final ItemLike icon,
                                    @NotNull final String name)
    {
        return new DisplayInfo(new ItemStack(icon),
                Component.translatable("advancements.minecolonies." + name + ".title"),
                Component.translatable("advancements.minecolonies." + name + ".description"),
                null, frame, true, true, false);
    }

    /**
     * Fabric's 1.20.1 advancement exporter cannot resolve a parent that is not
     * part of the same provider.  A metadata-only parent preserves the vanilla
     * parent identifier in the generated JSON without inventing a replacement
     * advancement.
     */
    private static Advancement externalParent(@NotNull final ResourceLocation id)
    {
        return new Advancement(id, null, null, AdvancementRewards.EMPTY, Map.of(), new String[0][], false);
    }

    private static DisplayInfo makeHidden(@NotNull final FrameType frame,
                                          @NotNull final ItemLike icon,
                                          @NotNull final String name)
    {
        return new DisplayInfo(new ItemStack(icon),
                Component.translatable("advancements.minecolonies." + name + ".title"),
                Component.translatable("advancements.minecolonies." + name + ".description"),
                null, frame, true, true, true);
    }

    private static ItemPredicate[] item(@NotNull final ItemLike item)
    {
        return new ItemPredicate[] { ItemPredicate.Builder.item().of(item).build() };
    }

    @NotNull
    private static CriterionTriggerInstance placeStructure(@NotNull final BuildingEntry building)
    {
        return new PlaceStructureCriterionInstance(building.getBuildingBlock().getBlueprintName());
    }

    @NotNull
    private static CriterionTriggerInstance createBuildRequest(@NotNull final BuildingEntry building, final int level)
    {
        return new CreateBuildRequestCriterionInstance(building.getBuildingBlock().getBlueprintName(), level);
    }

    @NotNull
    private static CriterionTriggerInstance completeBuildRequest(@NotNull final BuildingEntry building, final int level)
    {
        return new CompleteBuildRequestCriterionInstance(building.getBuildingBlock().getBlueprintName(), level);
    }
}
