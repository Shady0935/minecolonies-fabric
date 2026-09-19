package com.minecolonies.fabric.gametest;

import com.mojang.authlib.GameProfile;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.IMinimumStockModule;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.colony.permissions.Explosions;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.network.PacketUtils;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.DefaultBuildingInstance;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.citizen.VisitorCitizen;
import com.minecolonies.coremod.entity.CustomArrowEntity;
import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.entity.SpearEntity;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderDecoration;
import com.minecolonies.coremod.colony.buildings.modules.CourierAssignmentModule;
import com.minecolonies.coremod.colony.buildings.modules.DeliverymanAssignmentModule;
import com.minecolonies.coremod.colony.buildings.modules.BuildingModules;
import com.minecolonies.coremod.colony.buildings.modules.EntityListModule;
import com.minecolonies.coremod.colony.buildings.modules.GuardBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.MinerLevelManagementModule;
import com.minecolonies.coremod.colony.buildings.modules.QuarryModule;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDeliveryman;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGuardTower;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.colony.jobs.JobResearch;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.coremod.colony.managers.RaidManager;
import com.minecolonies.coremod.colony.fields.FarmField;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.DeliveryRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PickupRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.WarehouseRequestResolver;
import com.minecolonies.coremod.colony.workorders.WorkOrderMiner;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.minecolonies.coremod.entity.ai.citizen.miner.MinerLevel;
import com.minecolonies.coremod.network.NetworkChannel;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.coremod.network.messages.client.GlobalQuestSyncMessage;
import com.minecolonies.coremod.network.messages.client.OpenDecoBuildWindowMessage;
import com.minecolonies.coremod.network.messages.client.ServerUUIDMessage;
import com.minecolonies.coremod.network.messages.client.SaveStructureNBTMessage;
import com.minecolonies.coremod.network.messages.client.CreateColonyMessage;
import com.minecolonies.coremod.network.messages.splitting.SplitPacketMessage;
import com.minecolonies.coremod.network.messages.server.DecorationBuildRequestMessage;
import com.minecolonies.coremod.network.messages.server.DirectPlaceMessage;
import com.minecolonies.coremod.network.messages.server.ReactivateBuildingMessage;
import com.minecolonies.coremod.network.messages.server.colony.ColonyFlagChangeMessage;
import com.minecolonies.coremod.network.messages.server.colony.ColonyNameStyleMessage;
import com.minecolonies.coremod.network.messages.server.colony.ColonyStructureStyleMessage;
import com.minecolonies.coremod.network.messages.server.colony.ColonyTextureStyleMessage;
import com.minecolonies.coremod.network.messages.server.colony.ChangeFreeToInteractBlockMessage;
import com.minecolonies.coremod.network.messages.server.colony.TownHallRenameMessage;
import com.minecolonies.coremod.network.messages.server.colony.ToggleHousingMessage;
import com.minecolonies.coremod.network.messages.server.colony.ToggleJobMessage;
import com.minecolonies.coremod.network.messages.server.colony.WorkOrderChangeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.HutRenameMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.BuildRequestMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.BuildingSetStyleMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.ChangeDeliveryPriorityMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.CourierHiringModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.HireFireMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.AssignFilterableEntityMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.QuarryHiringModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.TransferItemsRequestMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.AddMinimumStockToBuildingModuleMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.RemoveMinimumStockFromBuildingModuleMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.TriggerSettingMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.home.AssignUnassignMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.builder.BuilderSelectWorkOrderMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.FarmFieldPlotResizeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.FarmFieldRegistrationMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.FarmFieldUpdateSeedMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.AssignFieldMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.AssignmentModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardSetMinePosMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.miner.MinerSetLevelMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.miner.MinerRepairLevelMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.university.TryResearchMessage;
import com.minecolonies.coremod.network.messages.server.colony.ToggleMoveInMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.AdjustSkillCitizenMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.PauseCitizenMessage;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.common.extensions.IForgeMenuType;
import com.minecolonies.fabric.event.Event;
import com.minecolonies.fabric.event.EventPriority;
import com.minecolonies.fabric.event.ForgeEventFactory;
import com.minecolonies.fabric.event.SubscribeEvent;
import com.minecolonies.fabric.event.entity.item.ItemTossEvent;
import com.minecolonies.fabric.event.entity.ProjectileImpactEvent;
import com.minecolonies.fabric.event.entity.living.LivingConversionEvent;
import com.minecolonies.fabric.event.entity.living.MobSpawnEvent;
import com.minecolonies.fabric.event.entity.player.ArrowNockEvent;
import com.minecolonies.fabric.event.entity.player.ArrowLooseEvent;
import com.minecolonies.fabric.event.entity.player.EntityItemPickupEvent;
import com.minecolonies.fabric.event.entity.player.ItemFishedEvent;
import com.minecolonies.fabric.event.level.BlockEvent;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import io.netty.buffer.Unpooled;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Focused server-side fixtures for the Fabric port.
 *
 * <p>The entrypoint is opt-in through Fabric API's GameTest runner. Normal
 * client and dedicated-server launches do not execute these methods.</p>
 */
public final class MineColoniesGameTests implements FabricGameTest
{
    private static final String TEST_BATCH = "minecolonies_fabric_port";
    private static final String ENTITY_TEST_BATCH = "minecolonies_fabric_entity_port";

    public MineColoniesGameTests()
    {
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void coreRegistriesAreAvailable(final GameTestHelper helper)
    {
        helper.assertTrue(ModBlocks.blockHutTownHall != null, "Town Hall block was not initialized");
        helper.assertTrue(IColonyManager.getInstance() != null, "Colony manager API is unavailable");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = ENTITY_TEST_BATCH, timeoutTicks = 200)
    public void customEntityTypesInstantiateAndReload(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        int customEntityCount = 0;

        for (final EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE)
        {
            final ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (!Constants.MOD_ID.equals(id.getNamespace()))
            {
                continue;
            }

            final Entity entity = entityType.create(level);
            helper.assertTrue(entity != null, "Could not instantiate custom entity type " + id);
            entity.setPos(helper.absolutePos(new BlockPos(1 + customEntityCount, 1, 1)).getCenter());

            final CompoundTag serialized = entity.saveWithoutId(new CompoundTag());
            helper.assertTrue(serialized.contains("Pos"), "Custom entity did not serialize its position: " + id);
            helper.assertTrue(entity.getType() == entityType, "Entity type mismatch after construction: " + id);
            final Entity reloaded = entityType.create(level);
            helper.assertTrue(reloaded != null, "Could not create NBT reload instance for " + id);
            reloaded.load(serialized);
            if (entity instanceof CustomArrowEntity || entity instanceof SpearEntity)
            {
                helper.assertTrue(reloaded.isRemoved(),
                  "Non-persistent projectile did not discard during NBT reload: " + id);
            }
            else
            {
                helper.assertTrue(reloaded.blockPosition().equals(entity.blockPosition()),
                  "Custom entity changed position during NBT reload: " + id);
            }
            entity.discard();
            reloaded.discard();
            customEntityCount++;
        }

        helper.assertTrue(customEntityCount == 25,
          "Expected 25 MineColonies entity types, found " + customEntityCount);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void eventBusHonorsPriorityAndInheritedListeners(final GameTestHelper helper)
    {
        final StringBuilder invocationOrder = new StringBuilder();
        final PriorityProbe probe = new PriorityProbe(invocationOrder);
        MinecraftForge.EVENT_BUS.register(probe);
        try
        {
            helper.assertTrue(!MinecraftForge.EVENT_BUS.post(new PriorityProbeEvent()),
              "Priority probe event was unexpectedly canceled");
            helper.assertTrue("highest|inherited-high|normal|low|lowest".contentEquals(invocationOrder),
              "Unexpected event listener order: " + invocationOrder);
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(probe);
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void researchDatapackBuildsUsableGlobalTree(final GameTestHelper helper)
    {
        final IGlobalResearchTree tree = IGlobalResearchTree.getInstance();
        final ResourceLocation civilian = new ResourceLocation(Constants.MOD_ID, "civilian");
        final ResourceLocation combat = new ResourceLocation(Constants.MOD_ID, "combat");
        final ResourceLocation technology = new ResourceLocation(Constants.MOD_ID, "technology");
        final ResourceLocation unlockable = new ResourceLocation(Constants.MOD_ID, "unlockable");

        helper.assertTrue(tree.getBranches().contains(civilian), "Civilian research branch was not loaded");
        helper.assertTrue(tree.getBranches().contains(combat), "Combat research branch was not loaded");
        helper.assertTrue(tree.getBranches().contains(technology), "Technology research branch was not loaded");
        helper.assertTrue(tree.getBranches().contains(unlockable), "Unlockable research branch was not loaded");
        helper.assertTrue(tree.getBranchData(civilian) != null, "Civilian branch metadata was not loaded");
        helper.assertTrue(tree.hasResearch(civilian, new ResourceLocation(Constants.MOD_ID, "civilian/academic")),
          "Civilian research entries were not parsed");
        helper.assertTrue(tree.hasResearch(combat, new ResourceLocation(Constants.MOD_ID, "combat/accuracy")),
          "Combat research entries were not parsed");
        helper.assertTrue(tree.hasResearch(technology, new ResourceLocation(Constants.MOD_ID, "technology/woodwork")),
          "Technology research entries were not parsed");
        helper.assertTrue(tree.hasResearch(unlockable, new ResourceLocation(Constants.MOD_ID, "unlockable/diamondmesh")),
          "Unlockable research entries were not parsed");
        helper.assertTrue(tree.hasResearchEffect(ResearchConstants.CITIZEN_CAP),
          "Default citizen-cap research effect was not registered");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void researchSelectionConsumesCostAndCompletes(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final Player player = makeNonCreativeResearchPlayer(level, townHall);
        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(!player.isCreative(), "Research fixture player unexpectedly remained creative");
        player.getInventory().clearContent();
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND));
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Research GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Research fixture colony was not created");

        final IGlobalResearchTree tree = IGlobalResearchTree.getInstance();
        final ResourceLocation branch = new ResourceLocation(Constants.MOD_ID, "civilian");
        final ResourceLocation researchId = new ResourceLocation(Constants.MOD_ID, "civilian/ambition");
        final IGlobalResearch research = tree.getResearch(branch, researchId);
        helper.assertTrue(research != null, "Research fixture entry was not loaded");
        helper.assertTrue(research.canResearch(1, colony.getResearchManager().getResearchTree()),
          "Level-one research was not eligible for a fresh colony");
        helper.assertTrue(player.getInventory().countItem(Items.DIAMOND) == 1,
          "Research fixture did not start with exactly one diamond");

        colony.getResearchManager().getResearchTree().attemptBeginResearch(player, colony, research);
        final ILocalResearch localResearch = colony.getResearchManager().getResearchTree().getResearch(branch, researchId);
        helper.assertTrue(localResearch != null, "Research selection did not create local research state");
        helper.assertTrue(localResearch.getState() == ResearchState.IN_PROGRESS,
          "Selected research did not enter the in-progress state");
        helper.assertTrue(player.getInventory().countItem(Items.DIAMOND) == 0,
          "Research selection did not consume its diamond cost");
        helper.assertTrue(colony.getResearchManager().getResearchTree().getResearchInProgress().size() == 1,
          "Selected research was not registered as in progress");

        final int requiredProgress = tree.getBranchData(branch).getBaseTime(research.getDepth());
        helper.assertTrue(requiredProgress > 0, "Research branch returned an invalid progress requirement");
        for (int progress = 0; progress < requiredProgress; progress++)
        {
            final boolean completed = localResearch.research(
              colony.getResearchManager().getResearchEffects(), colony.getResearchManager().getResearchTree());
            helper.assertTrue(completed == (progress + 1 == requiredProgress),
              "Research completion signal did not match its configured progress requirement");
        }

        helper.assertTrue(localResearch.getState() == ResearchState.FINISHED,
          "Research did not reach the finished state");
        helper.assertTrue(colony.getResearchManager().getResearchTree().getResearchInProgress().isEmpty(),
          "Finished research remained in the in-progress list");
        helper.assertTrue(colony.getResearchManager().getResearchTree().hasCompletedResearch(researchId),
          "Finished research was not recorded as completed");
        helper.assertTrue(colony.getResearchManager().getResearchEffects().getEffectStrength(
          new ResourceLocation(Constants.MOD_ID, "effects/blockhutmysticalsite")) > 0,
          "Finished research did not apply its configured effect");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void universityWorkerTickCompletesResearch(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeUniversity = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos universityPos = helper.absolutePos(relativeUniversity);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeUniversity, ModBlocks.blockHutUniversity);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric University GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "University fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "University fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        final IBuilding registeredTownHall = colony.getBuildingManager().addNewBuilding(townHallHut, level);
        helper.assertTrue(registeredTownHall != null && colony.hasTownHall(),
          "University fixture Town Hall was not registered");

        final BlockEntity blockEntity = level.getBlockEntity(universityPos);
        helper.assertTrue(blockEntity instanceof TileEntityColonyBuilding,
          "University fixture did not create a colony-building block entity");
        final TileEntityColonyBuilding universityHut = (TileEntityColonyBuilding) blockEntity;
        universityHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        universityHut.setBlueprintPath("education/university1.blueprint");
        universityHut.setSchematicName("university1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(universityHut, level);
        helper.assertTrue(registered instanceof BuildingUniversity,
          "University hut registered the wrong building implementation: " + registered);
        final BuildingUniversity university = (BuildingUniversity) registered;
        helper.assertTrue(university.getBuildingLevel() >= 1,
          "University fixture did not resolve its level-one blueprint");

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, universityPos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "University fixture could not create a live researcher citizen");
        final WorkerBuildingModule researcherModule = university.getModuleMatching(
          WorkerBuildingModule.class, module -> module.getJobEntry() == ModJobs.researcher.get());
        helper.assertTrue(researcherModule != null, "University researcher module was not registered");
        helper.assertTrue(researcherModule.assignCitizen(citizen),
          "University researcher module rejected the citizen assignment");
        helper.assertTrue(citizen.getJob() instanceof JobResearch,
          "University assignment did not create the researcher job");
        helper.assertTrue(researcherModule.getAssignedCitizen().size() == 1,
          "University researcher module did not retain the assigned citizen");

        final Player player = makeNonCreativeResearchPlayer(level, townHall);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND));
        final IGlobalResearchTree tree = IGlobalResearchTree.getInstance();
        final ResourceLocation branch = new ResourceLocation(Constants.MOD_ID, "civilian");
        final ResourceLocation researchId = new ResourceLocation(Constants.MOD_ID, "civilian/ambition");
        final IGlobalResearch research = tree.getResearch(branch, researchId);
        helper.assertTrue(research != null && research.canResearch(1, colony.getResearchManager().getResearchTree()),
          "University research fixture was not eligible to start");
        colony.getResearchManager().getResearchTree().attemptBeginResearch(player, colony, research);

        final ILocalResearch localResearch = colony.getResearchManager().getResearchTree().getResearch(branch, researchId);
        helper.assertTrue(localResearch != null && localResearch.getState() == ResearchState.IN_PROGRESS,
          "University research fixture did not enter the in-progress state");
        final int requiredProgress = tree.getBranchData(branch).getBaseTime(research.getDepth());
        helper.assertTrue(requiredProgress > 1, "University research fixture did not expose a multi-tick progress requirement");
        for (int progress = 0; progress < requiredProgress; progress++)
        {
            university.onColonyTick(colony);
            helper.assertTrue(localResearch.getProgress() == progress + 1 || localResearch.getState() == ResearchState.FINISHED,
              "University worker tick did not advance research progress at step " + (progress + 1));
        }
        helper.assertTrue(localResearch.getState() == ResearchState.FINISHED,
          "University worker tick did not advance research to completion");
        helper.assertTrue(colony.getResearchManager().getResearchTree().getResearchInProgress().isEmpty(),
          "University worker tick left completed research in progress");
        helper.assertTrue(colony.getResearchManager().getResearchTree().hasCompletedResearch(researchId),
          "University worker tick did not record completed research");
        helper.assertTrue(colony.getResearchManager().getResearchEffects().getEffectStrength(
          new ResourceLocation(Constants.MOD_ID, "effects/blockhutmysticalsite")) > 0,
          "University worker tick did not apply the research effect");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void createColonyMessageCreatesAndSerializesColony(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        helper.assertTrue(StructurePacks.hasPack(Constants.DEFAULT_STYLE),
          "Default structure pack was not discovered: " + Constants.DEFAULT_STYLE);
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final BlockState state = level.getBlockState(townHall);
        helper.assertTrue(state.getBlock() == ModBlocks.blockHutTownHall,
          "Town Hall block state was not installed: " + state);
        helper.assertTrue(state.hasBlockEntity(), "Town Hall block state does not expose a block entity");
        helper.assertTrue(MinecoloniesTileEntities.BUILDING.get().isValid(state),
          "Colony-building block entity type does not accept the Town Hall state");

        final BlockEntity blockEntity = level.getBlockEntity(townHall);
        helper.assertTrue(blockEntity instanceof TileEntityColonyBuilding,
          "Town Hall placement did not create a colony-building block entity");

        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final IColonyManager manager = IColonyManager.getInstance();
        final IColony created = manager.createColony(
          level, townHall, player, "Fabric GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(created != null, "Colony manager did not create a colony");

        final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) blockEntity;
        hut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        hut.setBlueprintPath("fundamentals/townhall1.blueprint");
        created.getBuildingManager().addNewBuilding(hut, level);

        final IColony colony = manager.getIColonyByOwner(level, player);
        helper.assertTrue(colony != null, "Created colony was not found by owner");
        helper.assertTrue(manager.getColonyByWorld(colony.getID(), level) == colony,
          "Created colony was not registered in the world manager");
        helper.assertTrue(colony.hasTownHall(), "Created colony has no Town Hall building");
        helper.assertTrue(colony.getBuildingManager().getBuilding(townHall) != null,
          "Town Hall block entity was not attached to the colony building manager");

        final CompoundTag saved = colony.getColonyTag();
        helper.assertTrue(saved != null && saved.contains("id") && saved.contains("name"),
          "Colony did not produce its persisted NBT payload");

        final Colony loaded = Colony.loadColony(saved.copy(), level);
        helper.assertTrue(loaded != null, "Persisted colony NBT could not be loaded");
        helper.assertTrue(loaded.getID() == colony.getID(), "Colony id changed during NBT round-trip");
        helper.assertTrue(loaded.getCenter().equals(colony.getCenter()),
          "Colony center changed during NBT round-trip");
        helper.assertTrue(loaded.getName().equals(colony.getName()),
          "Colony name changed during NBT round-trip");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void builderWorkOrderResolvesBlueprintAndRegisters(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, player, "Fabric Builder GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Builder fixture colony was not created");

        final BlockEntity blockEntity = level.getBlockEntity(townHall);
        helper.assertTrue(blockEntity instanceof TileEntityColonyBuilding,
          "Builder fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) blockEntity;
        hut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        hut.setBlueprintPath("fundamentals/townhall1.blueprint");
        colony.getBuildingManager().addNewBuilding(hut, level);
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 2, level, true);

        final IBuilding building = colony.getBuildingManager().getBuilding(townHall);
        helper.assertTrue(building != null, "Builder fixture Town Hall was not registered");
        final WorkOrderBuilding order = WorkOrderBuilding.create(WorkOrderType.BUILD, building);
        helper.assertTrue(order.getStructurePath().equals("fundamentals/townhall1.blueprint"),
          "Builder order selected the wrong blueprint path: " + order.getStructurePath());
        final Blueprint blueprint = StructurePacks.getBlueprint(order.getStructurePack(), order.getStructurePath());
        helper.assertTrue(blueprint != null,
          "Builder order could not resolve its blueprint");

        colony.getWorkManager().addWorkOrder(order, false);
        helper.assertTrue(order.getID() > 0, "Builder order did not receive a persistent id");
        helper.assertTrue(colony.getWorkManager().getWorkOrder(order.getID()) == order,
          "Builder order was not registered in the colony WorkManager");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void builderAssignsRegisteredWorkOrderToCitizen(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeBuilder = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos builderPos = helper.absolutePos(relativeBuilder);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeBuilder, ModBlocks.blockHutBuilder);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Builder Assignment GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Builder assignment fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "Builder assignment fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        final BlockEntity builderEntity = level.getBlockEntity(builderPos);
        helper.assertTrue(builderEntity instanceof TileEntityColonyBuilding,
          "Builder assignment fixture did not create a builder block entity");
        final TileEntityColonyBuilding builderHut = (TileEntityColonyBuilding) builderEntity;
        builderHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        builderHut.setBlueprintPath("fundamentals/builder1.blueprint");
        builderHut.setSchematicName("builder1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(builderHut, level);
        helper.assertTrue(registered instanceof BuildingBuilder,
          "Builder assignment fixture registered the wrong building implementation: " + registered);
        final BuildingBuilder builder = (BuildingBuilder) registered;
        helper.assertTrue(builder.getBuildingLevel() >= 1,
          "Builder assignment fixture did not resolve its level-one blueprint");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 2, level, true);

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, builderPos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "Builder assignment fixture could not create a live builder citizen");
        final WorkerBuildingModule workerModule = builder.getModuleMatching(
          WorkerBuildingModule.class, module -> module.getJobEntry() == ModJobs.builder.get());
        helper.assertTrue(workerModule != null, "Builder worker module was not registered");
        helper.assertTrue(workerModule.assignCitizen(citizen),
          "Builder worker module rejected the citizen assignment");
        helper.assertTrue(citizen.getJob() instanceof JobBuilder,
          "Builder assignment did not create the builder job");
        final JobBuilder job = citizen.getJob(JobBuilder.class);
        helper.assertTrue(job != null && !job.hasWorkOrder(),
          "Builder job unexpectedly started with a work order");

        final WorkOrderBuilding order = WorkOrderBuilding.create(WorkOrderType.BUILD, builder);
        colony.getWorkManager().addWorkOrder(order, false);
        helper.assertTrue(order.getID() > 0, "Builder assignment order did not receive a persistent id");
        helper.assertTrue(order.canBeMadeBy(job), "Builder assignment order rejected the assigned builder job");

        builder.searchWorkOrder();
        helper.assertTrue(job.hasWorkOrder() && job.getWorkOrder() == order,
          "Builder did not select the registered work order");
        helper.assertTrue(order.isClaimedBy(citizen),
          "Builder did not persist the work-order claim for its citizen");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void residenceAssignsHomeToCitizen(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeResidence = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos residencePos = helper.absolutePos(relativeResidence);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeResidence, ModBlocks.blockHutHome);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Residence GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Residence fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "Residence fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        final BlockEntity residenceEntity = level.getBlockEntity(residencePos);
        helper.assertTrue(residenceEntity instanceof TileEntityColonyBuilding,
          "Residence fixture did not create a colony-building block entity");
        final TileEntityColonyBuilding residenceHut = (TileEntityColonyBuilding) residenceEntity;
        residenceHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        residenceHut.setBlueprintPath("fundamentals/house1.blueprint");
        residenceHut.setSchematicName("house1");
        final IBuilding residence = colony.getBuildingManager().addNewBuilding(residenceHut, level);
        helper.assertTrue(residence != null && residence.hasModule(LivingBuildingModule.class),
          "Residence fixture did not register its living module: " + residence);
        helper.assertTrue(residence.getBuildingLevel() >= 1,
          "Residence fixture did not resolve its level-one house blueprint");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 2, level, true);

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, residencePos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "Residence fixture could not create a live citizen");
        final LivingBuildingModule livingModule = residence.getFirstModuleOccurance(LivingBuildingModule.class);
        helper.assertTrue(livingModule.assignCitizen(citizen),
          "Residence living module rejected the citizen assignment");
        helper.assertTrue(livingModule.hasAssignedCitizen(citizen),
          "Residence living module did not retain the assigned citizen");
        helper.assertTrue(citizen.getHomeBuilding() == residence,
          "Residence assignment did not update the citizen home building");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerAssignUnassignMessageUpdatesResidence(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeResidence = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos residencePos = helper.absolutePos(relativeResidence);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeResidence, ModBlocks.blockHutHome);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        helper.assertTrue(!owner.isCreative(), "C2S residence-assignment fixture owner remained creative");
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Residence Assignment Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S residence-assignment fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S residence-assignment fixture Town Hall did not create a block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S residence-assignment fixture Town Hall was not registered");

        final BlockEntity residenceEntity = level.getBlockEntity(residencePos);
        helper.assertTrue(residenceEntity instanceof TileEntityColonyBuilding,
          "C2S residence-assignment fixture did not create a residence block entity");
        final TileEntityColonyBuilding residenceHut = (TileEntityColonyBuilding) residenceEntity;
        residenceHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        residenceHut.setBlueprintPath("fundamentals/house1.blueprint");
        residenceHut.setSchematicName("house1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(residenceHut, level);
        helper.assertTrue(registered instanceof DefaultBuildingInstance,
          "C2S residence-assignment fixture registered the wrong building: " + registered);
        final DefaultBuildingInstance residence = (DefaultBuildingInstance) registered;
        final LivingBuildingModule livingModule = residence.getFirstModuleOccurance(LivingBuildingModule.class);
        helper.assertTrue(livingModule != null,
          "C2S residence-assignment fixture did not register its living module");
        helper.assertTrue(residence.getBuildingLevel() >= 1,
          "C2S residence-assignment fixture did not resolve its level-one house blueprint");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 2, level, true);

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, residencePos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "C2S residence-assignment fixture could not create a live citizen");
        helper.assertTrue(!livingModule.hasAssignedCitizen(citizen),
          "C2S residence-assignment fixture citizen was assigned before the packet route");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S residence-assignment fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, AssignUnassignMessage.class);
        helper.assertTrue(messageId > 0, "AssignUnassign message was not registered");
        final int assignCommunicationId = 0x4153484F;
        dispatchServerMessage(channel, server, owner, messageId, assignCommunicationId,
          new AssignUnassignMessage(colony.getDimension(), colony.getID(), residencePos,
            true, citizen.getId(), null));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(livingModule.hasAssignedCitizen(citizen),
              "AssignUnassign message did not assign the residence citizen");
            helper.assertTrue(citizen.getHomeBuilding() == residence,
              "AssignUnassign message did not update the citizen home building");
            helper.assertTrue(channel.getMessageCache().getIfPresent(assignCommunicationId) == null,
              "AssignUnassign assignment envelope remained in the split-packet cache");
            final int unassignCommunicationId = 0x41534855;
            dispatchServerMessage(channel, server, owner, messageId, unassignCommunicationId,
              new AssignUnassignMessage(colony.getDimension(), colony.getID(), residencePos,
                false, citizen.getId(), null));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(!livingModule.hasAssignedCitizen(citizen),
                  "AssignUnassign message did not remove the residence citizen");
                helper.assertTrue(citizen.getHomeBuilding() == null,
                  "AssignUnassign message did not clear the citizen home building");
                helper.assertTrue(channel.getMessageCache().getIfPresent(unassignCommunicationId) == null,
                  "AssignUnassign removal envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void warehouseRegistersCourierAndRequestResolvers(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeWarehouse = new BlockPos(10, 1, 2);
        final BlockPos relativeDeliveryman = new BlockPos(14, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos warehousePos = helper.absolutePos(relativeWarehouse);
        final BlockPos deliverymanPos = helper.absolutePos(relativeDeliveryman);
        for (int x = 0; x <= 18; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeWarehouse, ModBlocks.blockHutWareHouse);
        helper.setBlock(relativeDeliveryman, ModBlocks.blockHutDeliveryman);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Warehouse GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Warehouse fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "Warehouse fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        final BlockEntity warehouseEntity = level.getBlockEntity(warehousePos);
        helper.assertTrue(warehouseEntity instanceof TileEntityColonyBuilding,
          "Warehouse fixture did not create a warehouse block entity");
        final TileEntityColonyBuilding warehouseHut = (TileEntityColonyBuilding) warehouseEntity;
        warehouseHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        warehouseHut.setBlueprintPath("craftsmanship/storage/warehouse1.blueprint");
        warehouseHut.setSchematicName("warehouse1");
        final IBuilding warehouseBuilding = colony.getBuildingManager().addNewBuilding(warehouseHut, level);
        helper.assertTrue(warehouseBuilding instanceof BuildingWareHouse,
          "Warehouse fixture registered the wrong building implementation: " + warehouseBuilding);
        helper.assertTrue(warehouseBuilding.getBuildingLevel() >= 1,
          "Warehouse fixture did not resolve its level-one blueprint");

        final BlockEntity deliverymanEntity = level.getBlockEntity(deliverymanPos);
        helper.assertTrue(deliverymanEntity instanceof TileEntityColonyBuilding,
          "Warehouse fixture did not create a deliveryman block entity");
        final TileEntityColonyBuilding deliverymanHut = (TileEntityColonyBuilding) deliverymanEntity;
        deliverymanHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        deliverymanHut.setBlueprintPath("craftsmanship/storage/courier1.blueprint");
        deliverymanHut.setSchematicName("courier1");
        final IBuilding deliverymanBuilding = colony.getBuildingManager().addNewBuilding(deliverymanHut, level);
        helper.assertTrue(deliverymanBuilding instanceof BuildingDeliveryman,
          "Warehouse fixture registered the wrong deliveryman implementation: " + deliverymanBuilding);
        helper.assertTrue(deliverymanBuilding.getBuildingLevel() >= 1,
          "Warehouse fixture did not resolve its level-one courier blueprint");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, deliverymanPos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "Warehouse fixture could not create a live courier citizen");
        final WorkerBuildingModule courierWork = deliverymanBuilding.getModuleMatching(
          WorkerBuildingModule.class, module -> module.getJobEntry() == ModJobs.delivery.get());
        helper.assertTrue(courierWork instanceof DeliverymanAssignmentModule,
          "Deliveryman worker module was not registered");
        helper.assertTrue(courierWork.assignCitizen(citizen),
          "Deliveryman worker module rejected the citizen assignment");
        helper.assertTrue(citizen.getJob() instanceof JobDeliveryman,
          "Deliveryman assignment did not create the deliveryman job");

        final CourierAssignmentModule couriers = warehouseBuilding.getFirstModuleOccurance(CourierAssignmentModule.class);
        helper.assertTrue(couriers.assignCitizen(citizen),
          "Warehouse courier module rejected the assigned deliveryman");
        helper.assertTrue(couriers.hasAssignedCitizen(citizen),
          "Warehouse courier module did not retain the assigned deliveryman");
        helper.assertTrue(((BuildingWareHouse) warehouseBuilding).canAccessWareHouse(citizen),
          "Assigned deliveryman could not access the warehouse");

        final java.util.Collection<IRequestResolver<?>> resolvers = warehouseBuilding.createResolvers();
        helper.assertTrue(resolvers.stream().anyMatch(resolver -> resolver instanceof WarehouseRequestResolver),
          "Warehouse request resolver was not registered");
        helper.assertTrue(resolvers.stream().anyMatch(resolver -> resolver instanceof DeliveryRequestResolver),
          "Warehouse delivery resolver was not registered");
        helper.assertTrue(resolvers.stream().anyMatch(resolver -> resolver instanceof PickupRequestResolver),
          "Warehouse pickup resolver was not registered");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void minerAssignsRegisteredMineWorkOrderToCitizen(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeMiner = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos minerPos = helper.absolutePos(relativeMiner);
        for (int x = 0; x <= 16; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeMiner, ModBlocks.blockHutMiner);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Miner GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Miner fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "Miner fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        final BlockEntity minerEntity = level.getBlockEntity(minerPos);
        helper.assertTrue(minerEntity instanceof TileEntityColonyBuilding,
          "Miner fixture did not create a miner block entity");
        final TileEntityColonyBuilding minerHut = (TileEntityColonyBuilding) minerEntity;
        minerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        minerHut.setBlueprintPath("fundamentals/mine1.blueprint");
        minerHut.setSchematicName("mine1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(minerHut, level);
        helper.assertTrue(registered instanceof BuildingMiner,
          "Miner fixture registered the wrong building implementation: " + registered);
        final BuildingMiner miner = (BuildingMiner) registered;
        helper.assertTrue(miner.getBuildingLevel() >= 1,
          "Miner fixture did not resolve its level-one mine blueprint");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, minerPos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "Miner fixture could not create a live miner citizen");
        final WorkerBuildingModule workerModule = miner.getModuleMatching(
          WorkerBuildingModule.class, module -> module.getJobEntry() == ModJobs.miner.get());
        helper.assertTrue(workerModule != null, "Miner worker module was not registered");
        helper.assertTrue(workerModule.assignCitizen(citizen),
          "Miner worker module rejected the citizen assignment");
        helper.assertTrue(citizen.getJob() instanceof JobMiner,
          "Miner assignment did not create the miner job");
        final JobMiner job = citizen.getJob(JobMiner.class);
        final WorkOrderMiner order = new WorkOrderMiner(
          miner.getStructurePack(), "fundamentals/mine1.blueprint", "mine1", 0,
          minerPos.above(), false, miner.getID());
        colony.getWorkManager().addWorkOrder(order, false);
        helper.assertTrue(order.getID() > 0, "Miner work order did not receive a persistent id");
        helper.assertTrue(order.canBeMadeBy(job), "Miner work order rejected the assigned miner job");

        miner.searchWorkOrder();
        helper.assertTrue(job.hasWorkOrder() && job.getWorkOrder() == order,
          "Miner did not select the registered mine work order");
        helper.assertTrue(order.isClaimedBy(citizen),
          "Miner did not persist the work-order claim for its citizen");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void farmerRegistersAndAssignsSeededField(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeFarmer = new BlockPos(10, 1, 2);
        final BlockPos relativeField = new BlockPos(15, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos farmerPos = helper.absolutePos(relativeFarmer);
        final BlockPos fieldPos = helper.absolutePos(relativeField);
        for (int x = 0; x <= 18; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeFarmer, ModBlocks.blockHutFarmer);
        helper.setBlock(relativeField, ModBlocks.blockScarecrow);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Farmer GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Farmer fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "Farmer fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        final BlockEntity farmerEntity = level.getBlockEntity(farmerPos);
        helper.assertTrue(farmerEntity instanceof TileEntityColonyBuilding,
          "Farmer fixture did not create a farmer block entity");
        final TileEntityColonyBuilding farmerHut = (TileEntityColonyBuilding) farmerEntity;
        farmerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        farmerHut.setBlueprintPath("agriculture/horticulture/farm1.blueprint");
        farmerHut.setSchematicName("farm1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(farmerHut, level);
        helper.assertTrue(registered instanceof BuildingFarmer,
          "Farmer fixture registered the wrong building implementation: " + registered);
        final BuildingFarmer farmer = (BuildingFarmer) registered;
        helper.assertTrue(farmer.getBuildingLevel() >= 1,
          "Farmer fixture did not resolve its level-one farm blueprint");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final FarmField field = FarmField.create(fieldPos);
        field.setSeed(new ItemStack(Items.WHEAT));
        helper.assertTrue(field.isValidPlacement(colony),
          "Farmer fixture field does not have a valid scarecrow placement");
        helper.assertTrue(colony.getBuildingManager().addField(field),
          "Farmer fixture field was not registered in the colony");

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, farmerPos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "Farmer fixture could not create a live farmer citizen");
        final WorkerBuildingModule workerModule = farmer.getModuleMatching(
          WorkerBuildingModule.class, module -> module.getJobEntry() == ModJobs.farmer.get());
        helper.assertTrue(workerModule != null, "Farmer worker module was not registered");
        helper.assertTrue(workerModule.assignCitizen(citizen),
          "Farmer worker module rejected the citizen assignment");
        helper.assertTrue(citizen.getJob() instanceof JobFarmer,
          "Farmer assignment did not create the farmer job");

        final BuildingFarmer.FarmerFieldsModule fields = farmer.getFirstModuleOccurance(
          BuildingFarmer.FarmerFieldsModule.class);
        helper.assertTrue(fields.canAssignField(field),
          "Farmer fields module rejected a seeded farm field");
        fields.assignField(field);
        helper.assertTrue(fields.getOwnedFields().contains(field),
          "Farmer fields module did not retain the assigned field");
        helper.assertTrue(field.getBuildingId().equals(farmer.getID()),
          "Farmer field assignment did not persist the owning building");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void guardTowerAssignsKnightGuardToCitizen(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeGuardTower = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos guardTowerPos = helper.absolutePos(relativeGuardTower);
        for (int x = 0; x <= 16; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeGuardTower, ModBlocks.blockHutGuardTower);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Guard GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Guard fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "Guard fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        final BlockEntity guardTowerEntity = level.getBlockEntity(guardTowerPos);
        helper.assertTrue(guardTowerEntity instanceof TileEntityColonyBuilding,
          "Guard fixture did not create a guard-tower block entity");
        final TileEntityColonyBuilding guardTowerHut = (TileEntityColonyBuilding) guardTowerEntity;
        guardTowerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        guardTowerHut.setBlueprintPath("military/guardtower1.blueprint");
        guardTowerHut.setSchematicName("guardtower1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(guardTowerHut, level);
        helper.assertTrue(registered instanceof BuildingGuardTower,
          "Guard fixture registered the wrong building implementation: " + registered);
        final BuildingGuardTower guardTower = (BuildingGuardTower) registered;
        helper.assertTrue(guardTower.getBuildingLevel() >= 1,
          "Guard fixture did not resolve its level-one guard-tower blueprint");
        helper.assertTrue(guardTower.getGuardPos().equals(guardTower.getID()),
          "Guard tower did not initialize its default defense position");
        helper.assertTrue(guardTower.getTask().equals(GuardTaskSetting.PATROL),
          "Guard tower did not initialize its patrol task");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, guardTowerPos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "Guard fixture could not create a live guard citizen");
        final GuardBuildingModule guardModule = guardTower.getModuleMatching(
          GuardBuildingModule.class, module -> module.getJobEntry() == ModJobs.knight.get());
        helper.assertTrue(guardModule != null, "Guard tower knight module was not registered");
        helper.assertTrue(guardModule.assignCitizen(citizen),
          "Guard tower knight module rejected the citizen assignment");
        helper.assertTrue(citizen.getJob() instanceof JobKnight,
          "Guard tower assignment did not create the knight job");
        helper.assertTrue(guardModule.getAssignedCitizen().contains(citizen),
          "Guard tower knight module did not retain the assigned citizen");
        helper.assertTrue(guardTower.getAllAssignedCitizen().contains(citizen),
          "Guard tower did not expose the assigned citizen through its guard roster");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void raiderManagerStartsEligibleBarbarianEvent(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final boolean previousMobSpawning = level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(true, level.getServer());
        // Keep the raid colony in an isolated region.  The Fabric GameTest
        // runner places all empty templates in one shared server world, so
        // the small default fixture coordinates can otherwise make
        // RaidManager's legitimate other-colony guard reject outward spawn
        // candidates near another fixture.
        final BlockPos relativeTownHall = new BlockPos(256, 1, 256);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final int forcedChunkRadius = 12;
        final int townHallChunkX = townHall.getX() >> 4;
        final int townHallChunkZ = townHall.getZ() >> 4;
        for (int chunkX = townHallChunkX - forcedChunkRadius; chunkX <= townHallChunkX + forcedChunkRadius; chunkX++)
        {
            for (int chunkZ = townHallChunkZ - forcedChunkRadius; chunkZ <= townHallChunkZ + forcedChunkRadius; chunkZ++)
            {
                level.setChunkForced(chunkX, chunkZ, true);
            }
        }
        try
        {
        level.getChunkAt(townHall);
        for (int x = relativeTownHall.getX() - 2; x <= relativeTownHall.getX() + 14; x++)
        {
            for (int z = relativeTownHall.getZ() - 2; z <= relativeTownHall.getZ() + 12; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        // RaidManager advances in three-chunk steps and then resolves the
        // nearest solid floor.  Keep those real lookup rings inside the
        // deterministic GameTest world instead of relying on generated terrain
        // outside the entity-ticking fixture area.
        for (final int radius : new int[] {48, 96, 144})
        {
            for (int x = relativeTownHall.getX() - radius - 4; x <= relativeTownHall.getX() + radius + 4; x++)
            {
                for (int z = relativeTownHall.getZ() - radius - 4; z <= relativeTownHall.getZ() + radius + 4; z++)
                {
                    final int dx = x - relativeTownHall.getX();
                    final int dz = z - relativeTownHall.getZ();
                    final int distanceSquared = dx * dx + dz * dz;
                    if (distanceSquared >= (radius - 4) * (radius - 4)
                      && distanceSquared <= (radius + 4) * (radius + 4))
                    {
                        helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                    }
                }
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Raider GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Raider fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "Raider fixture Town Hall did not create a colony-building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        for (int i = 0; i < 8; i++)
        {
            final BlockPos citizenPos = new BlockPos(relativeTownHall.getX() + 2 + (i % 4) * 2,
              1, relativeTownHall.getZ() + 4 + (i / 4) * 2);
            final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, citizenPos);
            helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
              "Raider fixture could not create citizen " + i);
        }

        final IRaiderManager raiderManager = colony.getRaiderManager();
        helper.assertTrue(raiderManager.getColonyRaidLevel() >= RaidManager.MIN_REQUIRED_RAIDLEVEL,
          "Raider fixture did not reach the minimum colony raid level");
        helper.assertTrue(raiderManager.canRaid(true),
          "Raider fixture did not pass the forced raid eligibility gates: difficulty="
            + level.getDifficulty() + ", doMobSpawning=" + level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
            + ", importantPlayers=" + colony.getPackageManager().getImportantColonyPlayers().size());
        final IRaiderManager.RaidSpawnResult result = raiderManager.raiderEvent("barbarian", true, false);
        helper.assertTrue(result == IRaiderManager.RaidSpawnResult.SUCCESS,
          "Raider manager did not start the forced barbarian event: " + result);
        helper.assertTrue(!raiderManager.getLastSpawnPoints().isEmpty(),
          "Raider manager did not persist a spawn point for the forced event");

        final IColonyRaidEvent raidEvent = colony.getEventManager().getEvents().values().stream()
          .filter(IColonyRaidEvent.class::isInstance)
          .map(IColonyRaidEvent.class::cast)
          .findFirst()
          .orElse(null);
        helper.assertTrue(raidEvent != null, "Raider manager did not register the colony raid event");
        helper.assertTrue(raidEvent.getEventTypeID().equals(new ResourceLocation(Constants.MOD_ID, "barbarian_raid")),
          "Raider manager registered the wrong event type: " + raidEvent.getEventTypeID());
        helper.assertTrue(raidEvent.getSpawnPos() != null,
          "Raider event did not retain its calculated spawn position");
        helper.assertTrue(raidEvent.getStatus() == EventStatus.STARTING,
          "Raider event did not begin in the STARTING state");

        raiderManager.setCanHaveRaiderEvents(false);
        helper.assertTrue(!raiderManager.canRaid(true),
          "Raider manager ignored the disabled colony raid-events flag");
        helper.assertTrue(raiderManager.raiderEvent("barbarian", true, false) == IRaiderManager.RaidSpawnResult.CANNOT_RAID,
          "Raider manager started an event after colony raid-events were disabled");
        helper.succeed();
        }
        finally
        {
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(previousMobSpawning, level.getServer());
            for (int chunkX = townHallChunkX - forcedChunkRadius; chunkX <= townHallChunkX + forcedChunkRadius; chunkX++)
            {
                for (int chunkZ = townHallChunkZ - forcedChunkRadius; chunkZ <= townHallChunkZ + forcedChunkRadius; chunkZ++)
                {
                    level.setChunkForced(chunkX, chunkZ, false);
                }
            }
        }
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void citizenSpawnRegistersAndSerializesEntityData(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        for (int x = 0; x < 5; x++)
        {
            for (int z = 0; z < 5; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }

        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        final BlockEntity blockEntity = level.getBlockEntity(townHall);
        helper.assertTrue(blockEntity instanceof TileEntityColonyBuilding,
          "Citizen fixture Town Hall did not create a colony-building block entity");

        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final IColony created = IColonyManager.getInstance().createColony(
          level, townHall, player, "Fabric Citizen GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(created != null, "Citizen fixture colony was not created");

        final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) blockEntity;
        hut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        hut.setBlueprintPath("fundamentals/townhall1.blueprint");
        created.getBuildingManager().addNewBuilding(hut, level);

        final ICitizenData citizenData = created.getCitizenManager().spawnOrCreateCitizen(null, level, townHall.above());
        helper.assertTrue(citizenData != null, "Citizen manager did not create citizen data");
        helper.assertTrue(citizenData.getEntity().isPresent(), "Citizen data did not register a live entity");
        final AbstractEntityCitizen entity = citizenData.getEntity().orElse(null);
        helper.assertTrue(entity instanceof EntityCitizen, "Citizen data registered the wrong entity type");
        helper.assertTrue(entity.getCivilianID() == citizenData.getId(), "Citizen entity/data ids diverged");
        helper.assertTrue(entity.getCitizenData() == citizenData, "Citizen entity did not retain its data object");

        final CompoundTag citizenNBT = citizenData.serializeNBT();
        helper.assertTrue(citizenNBT.contains("id") && citizenNBT.contains("pos"),
          "Citizen data did not serialize identity and position");
        final CompoundTag colonyNBT = created.getColonyTag();
        final Colony loaded = Colony.loadColony(colonyNBT.copy(), level);
        final ICitizenData loadedCitizen = loaded.getCitizenManager().getCivilian(citizenData.getId());
        helper.assertTrue(loadedCitizen != null, "Colony NBT round-trip lost citizen data");
        helper.assertTrue(loadedCitizen.getName().equals(citizenData.getName()),
          "Colony NBT round-trip changed citizen name");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void supplyLootModifierAddsCampAndShipEntries(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 1, 1));
        final LootParams params = new LootParams.Builder(level)
          .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin))
          .create(LootContextParamSets.CHEST);
        final LootTable campTarget = level.getServer().getLootData().getLootTable(
          new ResourceLocation("minecraft", "chests/simple_dungeon"));
        final LootTable shipTarget = level.getServer().getLootData().getLootTable(
          new ResourceLocation("minecraft", "chests/shipwreck_supply"));
        helper.assertTrue(campTarget != null, "Vanilla camp target loot table was not loaded");
        helper.assertTrue(shipTarget != null, "Vanilla ship target loot table was not loaded");

        boolean campFound = false;
        boolean shipFound = false;
        for (int attempt = 0; attempt < 256 && (!campFound || !shipFound); attempt++)
        {
            if (!campFound)
            {
                for (final ItemStack stack : campTarget.getRandomItems(params))
                {
                    if (stack.is(ModItems.supplyCamp))
                    {
                        campFound = true;
                        helper.assertTrue(stack.hasTag() && "instant".equals(stack.getTag().getString("Placement")),
                          "Supply camp loot lost its instant-placement NBT");
                    }
                }
            }
            if (!shipFound)
            {
                for (final ItemStack stack : shipTarget.getRandomItems(params))
                {
                    if (stack.is(ModItems.supplyChest))
                    {
                        shipFound = true;
                        helper.assertTrue(stack.hasTag() && "instant".equals(stack.getTag().getString("Placement")),
                          "Supply ship loot lost its instant-placement NBT");
                    }
                }
            }
        }

        helper.assertTrue(campFound, "Supply camp loot was not added to a configured vanilla chest table");
        helper.assertTrue(shipFound, "Supply ship loot was not added to a configured vanilla chest table");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void colonyProtectionCallbackDeniesUnauthorizedTownHallAccess(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        // Keep this fixture outside the compact area reused by previous
        // GameTest runs; old colony protection listeners must not overlap it.
        final BlockPos relativeTownHall = new BlockPos(96, 1, 96);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        level.getChunkAt(townHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        final BlockEntity blockEntity = level.getBlockEntity(townHall);
        helper.assertTrue(blockEntity instanceof TileEntityColonyBuilding,
          "Protection fixture Town Hall did not create a colony-building block entity");

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Protection GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Protection fixture colony was not created");
        final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) blockEntity;
        hut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        hut.setBlueprintPath("fundamentals/townhall1.blueprint");
        colony.getBuildingManager().addNewBuilding(hut, level);

        final ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
        final BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(townHall), Direction.UP, townHall, false);
        final InteractionResult denied = UseBlockCallback.EVENT.invoker().interact(
          stranger, level, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(denied == InteractionResult.FAIL,
          "Unauthorized Town Hall interaction was not denied by the Fabric protection callback: " + denied);

        final InteractionResult allowed = UseBlockCallback.EVENT.invoker().interact(
          owner, level, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(allowed != InteractionResult.FAIL,
          "Colony owner was incorrectly denied by the Fabric protection callback");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void mobConversionCallbackDispatchesRetainedEvent(final GameTestHelper helper)
    {
        final AtomicBoolean eventSeen = new AtomicBoolean();
        MinecraftForge.EVENT_BUS.addListener(event ->
        {
            if (event instanceof com.minecolonies.fabric.event.entity.living.LivingConversionEvent.Pre)
            {
                eventSeen.set(true);
            }
        });

        final Mob previous = EntityType.ZOMBIE_VILLAGER.create(helper.getLevel());
        final Mob converted = EntityType.VILLAGER.create(helper.getLevel());
        helper.assertTrue(previous != null && converted != null, "Vanilla conversion fixtures could not be created");
        // The tavern fixture claims its own chunk in the same server-side
        // GameTest world.  Place this deliberately unrelated conversion well
        // outside that claim so the assertion tests event dispatch, not the
        // neighbouring colony's visitor rule.
        final BlockPos unrelatedConversionPos = helper.absolutePos(new BlockPos(256, 1, 256));
        previous.setPos(unrelatedConversionPos.getX() + 0.5, unrelatedConversionPos.getY(), unrelatedConversionPos.getZ() + 0.5);
        converted.setPos(previous.getX(), previous.getY(), previous.getZ());
        ServerLivingEntityEvents.MOB_CONVERSION.invoker().onConversion(previous, converted, true);
        helper.assertTrue(eventSeen.get(), "Fabric mob-conversion callback did not dispatch the retained event");
        helper.assertTrue(!converted.isRemoved(), "Unrelated conversion was canceled by the retained bridge");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void tavernConversionCreatesVisitorAndDiscardsVanillaCandidate(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        // Keep the conversion inside the GameTest entity-ticking area.  The
        // visitor manager intentionally defers spawns in simulation chunks
        // that are merely loaded but not ticking entities.
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeTavern = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos tavernPos = helper.absolutePos(relativeTavern);
        level.getChunkAt(townHall);
        level.getChunkAt(tavernPos);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeTavern, ModBlocks.blockHutTavern);

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        final BlockEntity tavernEntity = level.getBlockEntity(tavernPos);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "Visitor fixture Town Hall did not create a colony-building block entity");
        helper.assertTrue(tavernEntity instanceof TileEntityColonyBuilding,
          "Visitor fixture tavern did not create a colony-building block entity");

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Visitor GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Visitor fixture colony was not created");

        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);
        // A freshly placed level-one blueprint normally inherits the claim
        // made during the level-zero placement tick.  This direct fixture
        // creates the finished schematic in one call, so reproduce that claim
        // before resolving the conversion position through the colony manager.
        // The conversion handler resolves the colony through the chunk's
        // owning-colony entry, not merely through the list of nearby claims.
        // Reproduce the completed Town Hall claim with the same force-owner
        // path used by the in-game claim command.
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 0, level, true);

        final TileEntityColonyBuilding tavernHut = (TileEntityColonyBuilding) tavernEntity;
        tavernHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        tavernHut.setBlueprintPath("fundamentals/tavern1.blueprint");
        tavernHut.setSchematicName("tavern1");
        final IBuilding tavern = colony.getBuildingManager().addNewBuilding(tavernHut, level);
        helper.assertTrue(tavern != null && tavern.getBuildingLevel() >= 1,
          "Visitor fixture tavern was not registered at level one");
        helper.assertTrue(colony.hasBuilding("tavern", 1, false),
          "Visitor fixture colony did not expose its tavern to conversion logic");

        final BlockPos conversionPos = townHall.above(2);
        helper.assertTrue(IColonyManager.getInstance().getIColony(level, conversionPos) == colony,
          "Visitor conversion position did not resolve to the fixture colony");
        helper.assertTrue(WorldUtil.isEntityBlockLoaded(level, conversionPos),
          "Visitor conversion position is not in an entity-ticking chunk");
        owner.teleportTo(conversionPos.getX() + 0.5, conversionPos.getY(), conversionPos.getZ() + 0.5);
        helper.runAfterDelay(1, () ->
        {
            final net.minecraft.world.entity.monster.ZombieVillager previous =
              EntityType.ZOMBIE_VILLAGER.create(level);
            final Mob converted = EntityType.VILLAGER.create(level);
            helper.assertTrue(previous != null && converted != null, "Visitor conversion entities could not be created");
            previous.setPos(conversionPos.getX() + 0.5, conversionPos.getY(), conversionPos.getZ() + 0.5);
            converted.setPos(previous.getX(), previous.getY(), previous.getZ());
            final Set<Integer> visitorsBefore = new HashSet<>(colony.getVisitorManager().getCivilianDataMap().keySet());
            helper.assertTrue(level.addFreshEntity(previous), "Zombie Villager could not be added to the conversion fixture");

            // Other GameTests share this server world and may finish their
            // direct colony setup after this fixture was initialized.  The
            // conversion handler resolves the colony from the owning-colony
            // chunk entry, so refresh this fixture's completed Town Hall claim
            // at the actual conversion boundary and verify that lookup before
            // dispatching the Fabric callback.
            ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 0, level, true);
            final IColony resolvedColony = IColonyManager.getInstance().getIColony(level, conversionPos);
            helper.assertTrue(resolvedColony == colony && resolvedColony.hasBuilding("tavern", 1, false),
              "Visitor conversion chunk no longer resolves to its tavern colony: " + resolvedColony);

            final AtomicBoolean conversionCanceled = new AtomicBoolean(false);
            final Object conversionProbe = new Object()
            {
                @SubscribeEvent(priority = EventPriority.LOWEST)
                public void observe(final LivingConversionEvent.Pre event)
                {
                    if (event.getEntity() == previous)
                    {
                        conversionCanceled.set(event.isCanceled());
                    }
                }
            };
            MinecraftForge.EVENT_BUS.register(conversionProbe);
            try
            {
                ServerLivingEntityEvents.MOB_CONVERSION.invoker().onConversion(previous, converted, true);
            }
            finally
            {
                MinecraftForge.EVENT_BUS.unregister(conversionProbe);
            }

            helper.assertTrue(conversionCanceled.get(), "Tavern conversion event was not canceled by the retained handler");
            helper.assertTrue(converted.isRemoved(), "Tavern conversion left the vanilla candidate alive");
            final Set<Integer> visitorsAfter = new HashSet<>(colony.getVisitorManager().getCivilianDataMap().keySet());
            visitorsAfter.removeAll(visitorsBefore);
            helper.assertTrue(visitorsAfter.size() == 1,
              "Tavern conversion did not create exactly one visitor: " + visitorsAfter);
            final int visitorId = visitorsAfter.iterator().next();
            final var visitorData = colony.getVisitorManager().getVisitor(visitorId);
            helper.assertTrue(visitorData != null && visitorData.getHomeBuilding() == tavern,
              "Converted visitor was not assigned to the registered tavern");
            helper.assertTrue(visitorData.getEntity().isPresent() && visitorData.getEntity().get() instanceof VisitorCitizen,
              "Tavern conversion did not spawn a VisitorCitizen entity");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void pharaoScepterUseAndArrowLooseBridgePreserveForgeSemantics(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final ItemStack scepter = new ItemStack(ModItems.pharaoscepter);
        player.setItemInHand(InteractionHand.MAIN_HAND, scepter);

        final InteractionResultHolder<ItemStack> use = ModItems.pharaoscepter.use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(use.getResult() == InteractionResult.CONSUME,
          "Pharao scepter use did not return CONSUME: " + use.getResult());
        helper.assertTrue(player.isUsingItem(),
          "Pharao scepter use was short-circuited before startUsingItem");
        player.stopUsingItem();

        helper.assertTrue(ForgeEventFactory.onArrowNock(scepter, level, player, InteractionHand.MAIN_HAND, true) == null,
          "ArrowNockEvent bridge changed Forge's neutral no-listener result");

        final AtomicBoolean nockSeen = new AtomicBoolean();
        final Object actionListener = new Object()
        {
            @SubscribeEvent
            public void overrideArrowNock(final ArrowNockEvent event)
            {
                helper.assertTrue(event.getEntity() == player, "ArrowNockEvent exposed the wrong player");
                helper.assertTrue(event.getBow() == scepter, "ArrowNockEvent exposed the wrong item stack");
                helper.assertTrue(event.getLevel() == level, "ArrowNockEvent exposed the wrong level");
                helper.assertTrue(event.getHand() == InteractionHand.MAIN_HAND, "ArrowNockEvent exposed the wrong hand");
                helper.assertTrue(event.hasAmmo(), "ArrowNockEvent lost the has-ammo flag");
                event.setAction(new InteractionResultHolder<>(InteractionResult.SUCCESS, event.getBow()));
                nockSeen.set(true);
            }
        };
        MinecraftForge.EVENT_BUS.register(actionListener);
        try
        {
            final InteractionResultHolder<ItemStack> alternate = ForgeEventFactory.onArrowNock(
              scepter, level, player, InteractionHand.MAIN_HAND, true);
            helper.assertTrue(nockSeen.get(), "ArrowNockEvent bridge did not dispatch its event");
            helper.assertTrue(alternate != null && alternate.getResult() == InteractionResult.SUCCESS
                && alternate.getObject() == scepter,
              "ArrowNockEvent listener result was not returned to the caller");
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(actionListener);
        }

        final Object cancelNock = new Object()
        {
            @SubscribeEvent
            public void cancelArrowNock(final ArrowNockEvent event)
            {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.register(cancelNock);
        try
        {
            final InteractionResultHolder<ItemStack> canceled = ForgeEventFactory.onArrowNock(
              scepter, level, player, InteractionHand.MAIN_HAND, true);
            helper.assertTrue(canceled != null && canceled.getResult() == InteractionResult.FAIL
                && canceled.getObject() == scepter,
              "Canceled ArrowNockEvent did not return Forge's failure result");
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(cancelNock);
        }

        final Object cancelListener = new Object()
        {
            @SubscribeEvent
            public void cancelArrowLoose(final ArrowLooseEvent event)
            {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.register(cancelListener);
        try
        {
            final int charge = ForgeEventFactory.onArrowLoose(scepter, level, player, 20, true);
            helper.assertTrue(charge == -1,
              "Canceled ArrowLooseEvent did not stop the scepter shot: " + charge);
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(cancelListener);
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void extendedMenuTypesPreserveOpeningBuffer(final GameTestHelper helper)
    {
        helper.assertTrue(ModContainers.citizenInv.get() instanceof ExtendedScreenHandlerType,
          "Citizen inventory menu is not an extended Fabric screen handler type");
        helper.assertTrue(ModContainers.buildingInv.get() instanceof ExtendedScreenHandlerType,
          "Building inventory menu is not an extended Fabric screen handler type");
        helper.assertTrue(ModContainers.rackInv.get() instanceof ExtendedScreenHandlerType,
          "Rack menu is not an extended Fabric screen handler type");
        helper.assertTrue(ModContainers.graveInv.get() instanceof ExtendedScreenHandlerType,
          "Grave menu is not an extended Fabric screen handler type");
        helper.assertTrue(ModContainers.craftingFurnace.get() instanceof ExtendedScreenHandlerType,
          "Furnace crafting menu is not an extended Fabric screen handler type");
        helper.assertTrue(ModContainers.craftingGrid.get() instanceof ExtendedScreenHandlerType,
          "Crafting menu is not an extended Fabric screen handler type");
        helper.assertTrue(ModContainers.craftingBrewingstand.get() instanceof ExtendedScreenHandlerType,
          "Brewing menu is not an extended Fabric screen handler type");

        final MenuType<OpeningDataMenu> type = IForgeMenuType.create(
          (windowId, inventory, buffer) -> new OpeningDataMenu(windowId, buffer.readVarInt()));
        helper.assertTrue(type instanceof ExtendedScreenHandlerType,
          "IForgeMenuType did not create an extended screen handler type");

        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try
        {
            buffer.writeVarInt(731);
            @SuppressWarnings("unchecked")
            final ExtendedScreenHandlerType<OpeningDataMenu> extendedType =
              (ExtendedScreenHandlerType<OpeningDataMenu>) (ExtendedScreenHandlerType<?>) type;
            final OpeningDataMenu menu = extendedType.create(4, player.getInventory(), buffer);
            helper.assertTrue(menu.openingData == 731,
              "Extended screen handler factory did not receive the opening buffer");
        }
        finally
        {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void gameplayMixinsPreserveCancellableForgeEvents(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final CancellableGameplayEventsProbe probe = new CancellableGameplayEventsProbe();
        MinecraftForge.EVENT_BUS.register(probe);
        try
        {
            final ItemEntity pickedUp = new ItemEntity(level, player.getX(), player.getY(), player.getZ(),
              new ItemStack(net.minecraft.world.item.Items.DIRT));
            pickedUp.playerTouch(player);
            helper.assertTrue(probe.pickup, "ItemEntity.playerTouch did not dispatch EntityItemPickupEvent");
            helper.assertTrue(!pickedUp.isRemoved(), "Canceled item pickup removed the item entity");

            final ItemEntity tossed = player.drop(new ItemStack(net.minecraft.world.item.Items.DIRT), false, false);
            helper.assertTrue(probe.toss, "Player.drop did not dispatch ItemTossEvent");
            helper.assertTrue(tossed == null || tossed.isRemoved(),
              "Canceled item toss left a spawned item entity in the world");

            final BlockPos farmland = helper.absolutePos(new BlockPos(1, 1, 1));
            helper.setBlock(new BlockPos(1, 1, 1), Blocks.FARMLAND);
            ((FarmBlock) Blocks.FARMLAND).fallOn(level, level.getBlockState(farmland), farmland, player, 10.0F);
            helper.assertTrue(probe.trample, "FarmBlock.fallOn did not dispatch FarmlandTrampleEvent");
            helper.assertTrue(level.getBlockState(farmland).is(Blocks.FARMLAND),
              "Canceled farmland trample still converted farmland to dirt");

            final Mob hostile = EntityType.ZOMBIE.create(level);
            helper.assertTrue(hostile != null, "Mob spawn fixture could not create a zombie");
            hostile.setPos(player.getX(), player.getY(), player.getZ());
            final java.lang.reflect.Method spawnCheck = NaturalSpawner.class.getDeclaredMethod(
              "isValidPositionForMob", ServerLevel.class, Mob.class, double.class);
            spawnCheck.setAccessible(true);
            final boolean spawnAllowed = (boolean) spawnCheck.invoke(null, level, hostile, 0.0D);
            helper.assertTrue(probe.spawn, "NaturalSpawner did not dispatch MobSpawnEvent.PositionCheck");
            helper.assertTrue(!spawnAllowed, "Canceled mob position check allowed the hostile spawn");
        }
        catch (final ReflectiveOperationException exception)
        {
            throw new AssertionError("Could not invoke the vanilla mob spawn position gate", exception);
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(probe);
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void projectileImpactBridgePreservesCancellation(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final Entity projectile = EntityType.ARROW.create(level);
        helper.assertTrue(projectile != null, "Projectile impact fixture could not create an arrow");
        final HitResult hitResult = new net.minecraft.world.phys.BlockHitResult(
          new net.minecraft.world.phys.Vec3(0.5D, 1.0D, 0.5D), Direction.UP, BlockPos.ZERO, false);
        final AtomicBoolean eventSeen = new AtomicBoolean();
        final Object cancelListener = new Object()
        {
            @SubscribeEvent
            public void observe(final ProjectileImpactEvent event)
            {
                helper.assertTrue(event.getProjectile() == projectile,
                  "Projectile impact event exposed the wrong projectile");
                helper.assertTrue(event.getHitResult() == hitResult,
                  "Projectile impact event exposed the wrong hit result");
                eventSeen.set(true);
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.register(cancelListener);
        try
        {
            helper.assertTrue(ForgeEventFactory.onProjectileImpact(projectile, hitResult),
              "Canceled projectile impact did not return true");
            helper.assertTrue(eventSeen.get(), "Projectile impact bridge did not dispatch its event");
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(cancelListener);
            projectile.discard();
        }

        helper.assertTrue(!ForgeEventFactory.onProjectileImpact(projectile, hitResult),
          "Projectile impact bridge remained canceled after listener removal");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void fishingEventBridgePreservesRodDamageAndCancellation(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final NewBobberEntity hook = new NewBobberEntity(ModEntities.FISHHOOK, level);
        hook.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        final ItemStack fish = new ItemStack(Items.COD);
        final java.util.List<ItemStack> drops = java.util.List.of(fish);
        final AtomicBoolean eventSeen = new AtomicBoolean();
        final Object cancelListener = new Object()
        {
            @SubscribeEvent
            public void cancelFishing(final ItemFishedEvent event)
            {
                helper.assertTrue(event.getHookEntity() == hook, "ItemFishedEvent exposed the wrong hook");
                helper.assertTrue(event.getDrops().size() == 1 && event.getDrops().get(0) == fish,
                  "ItemFishedEvent did not preserve the generated drops");
                helper.assertTrue(event.getRodDamage() == 1, "ItemFishedEvent exposed the wrong default rod damage");
                event.damageRodBy(4);
                event.setCanceled(true);
                eventSeen.set(true);
            }
        };
        MinecraftForge.EVENT_BUS.register(cancelListener);
        try
        {
            final ItemFishedEvent canceled = ForgeEventFactory.onPlayerFishedItem(drops, 1, hook);
            helper.assertTrue(eventSeen.get(), "ItemFishedEvent bridge did not dispatch its event");
            helper.assertTrue(canceled.isCanceled() && canceled.getRodDamage() == 4,
              "ItemFishedEvent did not preserve cancellation and modified rod damage");
        }
        finally
        {
            MinecraftForge.EVENT_BUS.unregister(cancelListener);
            hook.discard();
        }

        final ItemFishedEvent neutral = ForgeEventFactory.onPlayerFishedItem(drops, 1, hook);
        helper.assertTrue(!neutral.isCanceled() && neutral.getRodDamage() == 1,
          "ItemFishedEvent bridge retained listener state after unregistering");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void explosionProtectionPoliciesPreserveStartAndDamageSemantics(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        level.getChunkAt(townHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        final BlockEntity blockEntity = level.getBlockEntity(townHall);
        helper.assertTrue(blockEntity instanceof TileEntityColonyBuilding,
          "Explosion protection Town Hall did not create a colony-building block entity");

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric Explosion GameTest Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "Explosion protection colony was not created");
        final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) blockEntity;
        hut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        hut.setBlueprintPath("fundamentals/townhall1.blueprint");
        colony.getBuildingManager().addNewBuilding(hut, level);
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 0, level, true);

        final Mob victim = EntityType.COW.create(level);
        helper.assertTrue(victim != null, "Explosion protection victim could not be created");
        victim.setPos(townHall.getX() + 0.5D, townHall.getY(), townHall.getZ() + 0.5D);
        final Explosions previousPolicy = MineColonies.getConfig().getServer().turnOffExplosionsInColonies.get();
        final boolean previousProtection = MineColonies.getConfig().getServer().enableColonyProtection.get();
        try
        {
            MineColonies.getConfig().getServer().enableColonyProtection.set(true);
            MineColonies.getConfig().getServer().turnOffExplosionsInColonies.set(Explosions.DAMAGE_PLAYERS);
            final var explosionSource = level.damageSources().explosion(null);
            final boolean protectedVictim = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker()
              .allowDamage(victim, explosionSource, 4.0F);
            helper.assertTrue(!protectedVictim,
              "DAMAGE_PLAYERS policy allowed explosion damage to a non-hostile colony entity");

            MineColonies.getConfig().getServer().turnOffExplosionsInColonies.set(Explosions.DAMAGE_ENTITIES);
            final boolean allowedVictim = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker()
              .allowDamage(victim, explosionSource, 4.0F);
            helper.assertTrue(allowedVictim,
              "DAMAGE_ENTITIES policy incorrectly blocked explosion damage to a colony entity");

            final BlockPos relativeFilteredBlock = new BlockPos(4, 1, 2);
            final BlockPos filteredBlock = helper.absolutePos(relativeFilteredBlock);
            helper.setBlock(relativeFilteredBlock, Blocks.STONE);
            MineColonies.getConfig().getServer().turnOffExplosionsInColonies.set(Explosions.DAMAGE_ENTITIES);
            level.explode(null, filteredBlock.getX() + 0.5D, filteredBlock.getY() + 0.5D,
              filteredBlock.getZ() + 0.5D, 2.0F, net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
            helper.assertTrue(level.getBlockState(filteredBlock).is(Blocks.STONE),
              "ExplosionEvent.Detonate did not filter a colony block under DAMAGE_ENTITIES");

            final BlockPos relativeProtectedItem = new BlockPos(8, 1, 2);
            final BlockPos protectedItemPos = helper.absolutePos(relativeProtectedItem);
            final ItemEntity protectedItem = new ItemEntity(level, protectedItemPos.getX() + 0.5D,
              protectedItemPos.getY(), protectedItemPos.getZ() + 0.5D,
              new ItemStack(Blocks.DIRT));
            level.addFreshEntity(protectedItem);
            MineColonies.getConfig().getServer().turnOffExplosionsInColonies.set(Explosions.DAMAGE_PLAYERS);
            level.explode(null, protectedItemPos.getX() + 0.5D, protectedItemPos.getY() + 0.5D,
              protectedItemPos.getZ() + 0.5D, 2.0F, net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
            helper.assertTrue(!protectedItem.isRemoved(),
              "ExplosionEvent.Detonate did not filter a non-living colony entity under DAMAGE_PLAYERS");

            final BlockPos relativeProtectedBlock = new BlockPos(6, 1, 2);
            final BlockPos protectedBlock = helper.absolutePos(relativeProtectedBlock);
            helper.setBlock(relativeProtectedBlock, Blocks.STONE);
            MineColonies.getConfig().getServer().turnOffExplosionsInColonies.set(Explosions.DAMAGE_NOTHING);
            level.explode(null, protectedBlock.getX() + 0.5D, protectedBlock.getY() + 0.5D,
              protectedBlock.getZ() + 0.5D, 2.0F, net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
            helper.assertTrue(level.getBlockState(protectedBlock).is(Blocks.STONE),
              "Canceled ExplosionEvent.Start still allowed a colony block explosion");
        }
        finally
        {
            MineColonies.getConfig().getServer().turnOffExplosionsInColonies.set(previousPolicy);
            MineColonies.getConfig().getServer().enableColonyProtection.set(previousProtection);
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void networkCodecsAndSplitEnvelopeRoundTrip(final GameTestHelper helper)
    {
        final NetworkChannel channel = Network.getNetwork();
        final ServerLevel level = helper.getLevel();
        helper.assertTrue(isMessageRegistered(channel, ServerUUIDMessage.class),
          "Server UUID message was not registered");
        helper.assertTrue(isMessageRegistered(channel, GlobalQuestSyncMessage.class),
          "Global quest message was not registered on the server");
        helper.assertTrue(isMessageRegistered(channel, OpenDecoBuildWindowMessage.class),
          "Build-window message was not registered on the server");
        helper.assertTrue(isMessageRegistered(channel, SaveStructureNBTMessage.class),
          "Scan-save message was not registered on the server");

        final EntityCitizen citizen = (EntityCitizen) ModEntities.CITIZEN.create(level);
        helper.assertTrue(citizen != null, "Fishing hook fixture could not create a citizen entity");
        level.addFreshEntity(citizen);
        final NewBobberEntity sourceHook = new NewBobberEntity(ModEntities.FISHHOOK, level);
        sourceHook.setAngler(citizen, 0, 0);
        final NewBobberEntity syncedHook = new NewBobberEntity(ModEntities.FISHHOOK, level);
        syncedHook.getEntityData().assignValues(sourceHook.getEntityData().getNonDefaultValues());
        helper.assertTrue(syncedHook.getAnglerId() == citizen.getId(),
          "Fabric fishing-hook spawn data did not preserve the angler entity id");

        final OpenDecoBuildWindowMessage original = new OpenDecoBuildWindowMessage(
          new BlockPos(11, 64, -7), Constants.DEFAULT_STYLE, "fundamentals/townhall1.blueprint",
          Rotation.CLOCKWISE_90, Mirror.FRONT_BACK);
        final byte[] encoded = encode(original);
        final OpenDecoBuildWindowMessage decoded = new OpenDecoBuildWindowMessage();
        final FriendlyByteBuf decodeBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(encoded));
        try
        {
            decoded.fromBytes(decodeBuffer);
        }
        finally
        {
            decodeBuffer.release();
        }
        helper.assertTrue(Arrays.equals(encoded, encode(decoded)),
          "OpenDecoBuildWindowMessage changed during codec round-trip");

        final int serverUuidId = findMessageId(channel, ServerUUIDMessage.class);
        helper.assertTrue(serverUuidId > 0, "Server UUID message has no inner network id");
        final UUID expected = UUID.fromString("11111111-2222-3333-4444-555555555555");
        final FriendlyByteBuf uuidBuffer = new FriendlyByteBuf(Unpooled.buffer());
        PacketUtils.writeUUID(uuidBuffer, expected);
        final byte[] uuidPayload = new byte[uuidBuffer.readableBytes()];
        uuidBuffer.getBytes(uuidBuffer.readerIndex(), uuidPayload);
        uuidBuffer.release();

        final int communicationId = 0x4D435446;
        final byte[] firstChunk = Arrays.copyOfRange(uuidPayload, 0, 7);
        final byte[] secondChunk = Arrays.copyOfRange(uuidPayload, 7, uuidPayload.length);
        final NetworkEvent.Context context = new NetworkEvent.Context(null, LogicalSide.SERVER);
        new SplitPacketMessage(communicationId, 1, false, serverUuidId, secondChunk).onExecute(context, false);
        new SplitPacketMessage(communicationId, 0, true, serverUuidId, firstChunk).onExecute(context, false);

        helper.assertTrue(IColonyManager.getInstance().getServerUUID().equals(expected),
          "Split packet reassembly did not deliver the UUID payload");
        helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
          "Completed split packet was not removed from the cache");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerEnvelopeReachesColonyHandler(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Envelope Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S fixture colony was not created");
        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S fixture has no running server");

        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, TownHallRenameMessage.class);
        helper.assertTrue(messageId > 0, "Town Hall rename message was not registered");
        final TownHallRenameMessage original = new TownHallRenameMessage(
          colony.getDimension(), colony.getID(), "C2S Routed Colony");
        final int communicationId = 0x43525348;
        final SplitPacketMessage envelope = new SplitPacketMessage(
          communicationId, 0, true, messageId, encode(original));
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.wrappedBuffer(encode(envelope)));
        try
        {
            channel.getRawChannel().handleServerPacket(server, owner, packet);
        }
        finally
        {
            packet.release();
        }

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue("C2S Routed Colony".equals(colony.getName()),
              "C2S envelope did not execute the Town Hall rename handler: " + colony.getName());
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "C2S envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerHutRenameMessageRenamesBuilding(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Hut Rename Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S hut rename fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S hut rename fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        final IBuilding registeredTownHall = colony.getBuildingManager().addNewBuilding(townHallHut, level);
        helper.assertTrue(registeredTownHall != null && colony.hasTownHall(),
          "C2S hut rename fixture Town Hall was not registered");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S hut rename fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, HutRenameMessage.class);
        helper.assertTrue(messageId > 0, "HutRename message was not registered");
        final HutRenameMessage original = new HutRenameMessage(
          colony.getDimension(), colony.getID(), townHall, "C2S Renamed Town Hall");
        final int communicationId = 0x4852544E;
        final SplitPacketMessage envelope = new SplitPacketMessage(
          communicationId, 0, true, messageId, encode(original));
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.wrappedBuffer(encode(envelope)));
        try
        {
            channel.getRawChannel().handleServerPacket(server, owner, packet);
        }
        finally
        {
            packet.release();
        }

        helper.runAfterDelay(1, () ->
        {
            final IBuilding renamedTownHall = colony.getBuildingManager().getBuilding(townHall);
            helper.assertTrue(renamedTownHall != null
              && "C2S Renamed Town Hall".equals(renamedTownHall.getCustomName()),
              "HutRename message did not rename the resolved building: "
                + (renamedTownHall == null ? null : renamedTownHall.getCustomName()));
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "HutRename envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerColonyStyleMessagesUpdateStyles(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Style Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S colony-style fixture colony was not created");
        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S colony-style fixture has no running server");

        final NetworkChannel channel = Network.getNetwork();
        final int nameStyleId = findMessageId(channel, ColonyNameStyleMessage.class);
        final int structureStyleId = findMessageId(channel, ColonyStructureStyleMessage.class);
        final int textureStyleId = findMessageId(channel, ColonyTextureStyleMessage.class);
        helper.assertTrue(nameStyleId > 0, "ColonyNameStyle message was not registered");
        helper.assertTrue(structureStyleId > 0, "ColonyStructureStyle message was not registered");
        helper.assertTrue(textureStyleId > 0, "ColonyTextureStyle message was not registered");

        final int nameCommunicationId = 0x43534E4D;
        final int structureCommunicationId = 0x4353534D;
        final int textureCommunicationId = 0x4353544D;
        dispatchServerMessage(channel, server, owner, nameStyleId, nameCommunicationId,
          new ColonyNameStyleMessage(colony.getDimension(), colony.getID(), "c2s_name_style"));
        dispatchServerMessage(channel, server, owner, structureStyleId, structureCommunicationId,
          new ColonyStructureStyleMessage(colony.getDimension(), colony.getID(), "c2s_structure_style"));
        dispatchServerMessage(channel, server, owner, textureStyleId, textureCommunicationId,
          new ColonyTextureStyleMessage(colony.getDimension(), colony.getID(), "c2s_texture_style"));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue("c2s_name_style".equals(colony.getNameStyle()),
              "ColonyNameStyle message did not update the colony name style: " + colony.getNameStyle());
            helper.assertTrue("c2s_structure_style".equals(colony.getStructurePack()),
              "ColonyStructureStyle message did not update the structure pack: " + colony.getStructurePack());
            helper.assertTrue("c2s_texture_style".equals(colony.getTextureStyleId()),
              "ColonyTextureStyle message did not update the texture style: " + colony.getTextureStyleId());
            helper.assertTrue(channel.getMessageCache().getIfPresent(nameCommunicationId) == null,
              "ColonyNameStyle envelope remained in the split-packet cache");
            helper.assertTrue(channel.getMessageCache().getIfPresent(structureCommunicationId) == null,
              "ColonyStructureStyle envelope remained in the split-packet cache");
            helper.assertTrue(channel.getMessageCache().getIfPresent(textureCommunicationId) == null,
              "ColonyTextureStyle envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerColonyManagementMessagesUpdateAllocationAndFlag(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Colony Management Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S colony-management fixture colony was not created");
        colony.setManualHousing(false);
        colony.setManualHiring(false);

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S colony-management fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int housingId = findMessageId(channel, ToggleHousingMessage.class);
        final int jobId = findMessageId(channel, ToggleJobMessage.class);
        final int flagId = findMessageId(channel, ColonyFlagChangeMessage.class);
        helper.assertTrue(housingId > 0, "ToggleHousing message was not registered");
        helper.assertTrue(jobId > 0, "ToggleJob message was not registered");
        helper.assertTrue(flagId > 0, "ColonyFlagChange message was not registered");

        final ListTag patterns = new ListTag();
        final CompoundTag pattern = new CompoundTag();
        pattern.putString("Pattern", "bs");
        pattern.putInt("Color", 1);
        patterns.add(pattern);

        final int housingCommunicationId = 0x43484F55;
        final int jobCommunicationId = 0x43484F4A;
        final int flagCommunicationId = 0x43484F46;
        dispatchServerMessage(channel, server, owner, housingId, housingCommunicationId,
          new ToggleHousingMessage(colony.getDimension(), colony.getID(), true));
        dispatchServerMessage(channel, server, owner, jobId, jobCommunicationId,
          new ToggleJobMessage(colony.getDimension(), colony.getID(), true));
        dispatchServerMessage(channel, server, owner, flagId, flagCommunicationId,
          new ColonyFlagChangeMessage(colony.getDimension(), colony.getID(), patterns));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(colony.isManualHousing(),
              "ToggleHousing message did not enable manual housing allocation");
            helper.assertTrue(colony.isManualHiring(),
              "ToggleJob message did not enable manual hiring allocation");
            helper.assertTrue(colony.getColonyFlag().equals(patterns),
              "ColonyFlagChange message did not preserve the selected banner patterns: "
                + colony.getColonyFlag());
            helper.assertTrue(channel.getMessageCache().getIfPresent(housingCommunicationId) == null,
              "ToggleHousing envelope remained in the split-packet cache");
            helper.assertTrue(channel.getMessageCache().getIfPresent(jobCommunicationId) == null,
              "ToggleJob envelope remained in the split-packet cache");
            helper.assertTrue(channel.getMessageCache().getIfPresent(flagCommunicationId) == null,
              "ColonyFlagChange envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerChangeFreeToInteractMessageUpdatesPermissions(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos freePosition = helper.absolutePos(new BlockPos(7, 1, 7));
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        helper.assertTrue(!owner.isCreative(), "C2S free-interaction fixture owner remained creative");
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Free Interaction Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S free-interaction fixture colony was not created");
        helper.assertTrue(colony instanceof Colony, "C2S free-interaction fixture used an unexpected colony implementation");
        final Colony colonyData = (Colony) colony;

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S free-interaction fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, ChangeFreeToInteractBlockMessage.class);
        helper.assertTrue(messageId > 0, "ChangeFreeToInteractBlock message was not registered");
        final int addBlockCommunicationId = 0x46524942;
        final int addPositionCommunicationId = 0x46524950;
        dispatchServerMessage(channel, server, owner, messageId, addBlockCommunicationId,
          new ChangeFreeToInteractBlockMessage(colony.getDimension(), colony.getID(),
            Blocks.CHEST, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
        dispatchServerMessage(channel, server, owner, messageId, addPositionCommunicationId,
          new ChangeFreeToInteractBlockMessage(colony.getDimension(), colony.getID(),
            freePosition, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(colonyData.getFreeBlocks().contains(Blocks.CHEST),
              "ChangeFreeToInteractBlock message did not add the free block");
            helper.assertTrue(colonyData.getFreePositions().contains(freePosition),
              "ChangeFreeToInteractBlock message did not add the free position");
            helper.assertTrue(channel.getMessageCache().getIfPresent(addBlockCommunicationId) == null,
              "ChangeFreeToInteractBlock block-add envelope remained in the split-packet cache");
            helper.assertTrue(channel.getMessageCache().getIfPresent(addPositionCommunicationId) == null,
              "ChangeFreeToInteractBlock position-add envelope remained in the split-packet cache");
            final int removeBlockCommunicationId = 0x46524952;
            final int removePositionCommunicationId = 0x46524953;
            dispatchServerMessage(channel, server, owner, messageId, removeBlockCommunicationId,
              new ChangeFreeToInteractBlockMessage(colony.getDimension(), colony.getID(),
                Blocks.CHEST, ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK));
            dispatchServerMessage(channel, server, owner, messageId, removePositionCommunicationId,
              new ChangeFreeToInteractBlockMessage(colony.getDimension(), colony.getID(),
                freePosition, ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(!colonyData.getFreeBlocks().contains(Blocks.CHEST),
                  "ChangeFreeToInteractBlock message did not remove the free block");
                helper.assertTrue(!colonyData.getFreePositions().contains(freePosition),
                  "ChangeFreeToInteractBlock message did not remove the free position");
                helper.assertTrue(channel.getMessageCache().getIfPresent(removeBlockCommunicationId) == null,
                  "ChangeFreeToInteractBlock block-remove envelope remained in the split-packet cache");
                helper.assertTrue(channel.getMessageCache().getIfPresent(removePositionCommunicationId) == null,
                  "ChangeFreeToInteractBlock position-remove envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerChangeDeliveryPriorityMessageUpdatesBuilder(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeBuilder = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos builderPos = helper.absolutePos(relativeBuilder);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeBuilder, ModBlocks.blockHutBuilder);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Delivery Priority Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S delivery-priority fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S delivery-priority fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S delivery-priority fixture Town Hall was not registered");

        final BlockEntity builderEntity = level.getBlockEntity(builderPos);
        helper.assertTrue(builderEntity instanceof TileEntityColonyBuilding,
          "C2S delivery-priority fixture did not create a Builder block entity");
        final TileEntityColonyBuilding builderHut = (TileEntityColonyBuilding) builderEntity;
        builderHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        builderHut.setBlueprintPath("fundamentals/builder1.blueprint");
        builderHut.setSchematicName("builder1");
        final IBuilding builder = colony.getBuildingManager().addNewBuilding(builderHut, level);
        helper.assertTrue(builder instanceof BuildingBuilder,
          "C2S delivery-priority fixture registered the wrong building: " + builder);
        helper.assertTrue(builder.hasModule(WorkerBuildingModule.class),
          "C2S delivery-priority fixture Builder has no worker module");
        final int initialPriority = builder.getPickUpPriority();

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S delivery-priority fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, ChangeDeliveryPriorityMessage.class);
        helper.assertTrue(messageId > 0, "ChangeDeliveryPriority message was not registered");
        final int increaseCommunicationId = 0x43504455;
        final int decreaseCommunicationId = 0x43504444;
        dispatchServerMessage(channel, server, owner, messageId, increaseCommunicationId,
          new ChangeDeliveryPriorityMessage(colony.getDimension(), colony.getID(), builderPos, true));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(builder.getPickUpPriority() == initialPriority + 1,
              "ChangeDeliveryPriority message did not increase the Builder priority: "
                + builder.getPickUpPriority());
            helper.assertTrue(channel.getMessageCache().getIfPresent(increaseCommunicationId) == null,
              "ChangeDeliveryPriority increase envelope remained in the split-packet cache");
            dispatchServerMessage(channel, server, owner, messageId, decreaseCommunicationId,
              new ChangeDeliveryPriorityMessage(colony.getDimension(), colony.getID(), builderPos, false));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(builder.getPickUpPriority() == initialPriority,
                  "ChangeDeliveryPriority message did not decrease the Builder priority: "
                    + builder.getPickUpPriority());
                helper.assertTrue(channel.getMessageCache().getIfPresent(decreaseCommunicationId) == null,
                  "ChangeDeliveryPriority decrease envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerTransferItemsRequestMovesItemsToBuilder(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeBuilder = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos builderPos = helper.absolutePos(relativeBuilder);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeBuilder, ModBlocks.blockHutBuilder);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        helper.assertTrue(!owner.isCreative(), "C2S transfer-items fixture owner remained creative");
        owner.getInventory().clearContent();
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Transfer Items Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S transfer-items fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S transfer-items fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S transfer-items fixture Town Hall was not registered");

        final BlockEntity builderEntity = level.getBlockEntity(builderPos);
        helper.assertTrue(builderEntity instanceof TileEntityColonyBuilding,
          "C2S transfer-items fixture did not create a Builder block entity");
        final TileEntityColonyBuilding builderHut = (TileEntityColonyBuilding) builderEntity;
        builderHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        builderHut.setBlueprintPath("fundamentals/builder1.blueprint");
        builderHut.setSchematicName("builder1");
        final IBuilding builder = colony.getBuildingManager().addNewBuilding(builderHut, level);
        helper.assertTrue(builder instanceof BuildingBuilder,
          "C2S transfer-items fixture registered the wrong building: " + builder);
        helper.assertTrue(builderHut.getInventory().getSlots() > 0,
          "C2S transfer-items fixture Builder has no inventory slots");

        final int quantity = 8;
        final ItemStack transferStack = new ItemStack(Items.COBBLESTONE, quantity);
        helper.assertTrue(owner.getInventory().add(transferStack.copy()),
          "C2S transfer-items fixture could not seed the owner inventory");
        helper.assertTrue(owner.getInventory().countItem(Items.COBBLESTONE) == quantity,
          "C2S transfer-items fixture did not seed the requested item quantity");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S transfer-items fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, TransferItemsRequestMessage.class);
        helper.assertTrue(messageId > 0, "TransferItemsRequest message was not registered");
        final int communicationId = 0x54495251;
        dispatchServerMessage(channel, server, owner, messageId, communicationId,
          new TransferItemsRequestMessage(colony.getDimension(), colony.getID(), builderPos,
            transferStack, quantity, false));

        helper.runAfterDelay(1, () ->
        {
            int storedCount = 0;
            for (int slot = 0; slot < builderHut.getInventory().getSlots(); slot++)
            {
                final ItemStack storedStack = builderHut.getInventory().getStackInSlot(slot);
                if (storedStack.is(Items.COBBLESTONE))
                {
                    storedCount += storedStack.getCount();
                }
            }
            helper.assertTrue(storedCount == quantity,
              "TransferItemsRequest message did not move the requested items into the Builder inventory: "
                + storedCount);
            helper.assertTrue(owner.getInventory().countItem(Items.COBBLESTONE) == 0,
              "TransferItemsRequest message did not remove the transferred items from the owner");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "TransferItemsRequest envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerMinimumStockMessagesUpdateBuilderModule(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeBuilder = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos builderPos = helper.absolutePos(relativeBuilder);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeBuilder, ModBlocks.blockHutBuilder);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Minimum Stock Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S minimum-stock fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S minimum-stock fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S minimum-stock fixture Town Hall was not registered");

        final BlockEntity builderEntity = level.getBlockEntity(builderPos);
        helper.assertTrue(builderEntity instanceof TileEntityColonyBuilding,
          "C2S minimum-stock fixture did not create a Builder block entity");
        final TileEntityColonyBuilding builderHut = (TileEntityColonyBuilding) builderEntity;
        builderHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        builderHut.setBlueprintPath("fundamentals/builder1.blueprint");
        builderHut.setSchematicName("builder1");
        final IBuilding builder = colony.getBuildingManager().addNewBuilding(builderHut, level);
        helper.assertTrue(builder instanceof BuildingBuilder,
          "C2S minimum-stock fixture registered the wrong building: " + builder);
        final IMinimumStockModule stockModule = builder.getModule(BuildingModules.MIN_STOCK);
        helper.assertTrue(stockModule != null,
          "C2S minimum-stock fixture Builder did not register the minimum-stock module");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S minimum-stock fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int addMessageId = findMessageId(channel, AddMinimumStockToBuildingModuleMessage.class);
        final int removeMessageId = findMessageId(channel, RemoveMinimumStockFromBuildingModuleMessage.class);
        helper.assertTrue(addMessageId > 0, "AddMinimumStock message was not registered");
        helper.assertTrue(removeMessageId > 0, "RemoveMinimumStock message was not registered");
        final ItemStack stockStack = new ItemStack(Items.OAK_PLANKS, 1);
        final int addCommunicationId = 0x4D535441;
        dispatchServerMessage(channel, server, owner, addMessageId, addCommunicationId,
          new AddMinimumStockToBuildingModuleMessage(colony.getDimension(), colony.getID(), builderPos,
            stockStack, 3));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(stockModule.isStocked(stockStack),
              "AddMinimumStock message did not register the Builder minimum-stock item");
            helper.assertTrue(channel.getMessageCache().getIfPresent(addCommunicationId) == null,
              "AddMinimumStock envelope remained in the split-packet cache");
            final int removeCommunicationId = 0x4D535452;
            dispatchServerMessage(channel, server, owner, removeMessageId, removeCommunicationId,
              new RemoveMinimumStockFromBuildingModuleMessage(colony.getDimension(), colony.getID(), builderPos,
                stockStack, BuildingModules.MIN_STOCK.getRuntimeID()));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(!stockModule.isStocked(stockStack),
                  "RemoveMinimumStock message did not remove the Builder minimum-stock item");
                helper.assertTrue(channel.getMessageCache().getIfPresent(removeCommunicationId) == null,
                  "RemoveMinimumStock envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerWorkOrderChangeMessageUpdatesAndRemovesOrder(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Work Order Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S work-order fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S work-order fixture did not create a Town Hall block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        final IBuilding building = colony.getBuildingManager().addNewBuilding(townHallHut, level);
        helper.assertTrue(building != null, "C2S work-order fixture Town Hall was not registered");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);
        final WorkOrderBuilding order = WorkOrderBuilding.create(WorkOrderType.BUILD, building);
        colony.getWorkManager().addWorkOrder(order, false);
        helper.assertTrue(order.getID() > 0, "C2S work-order fixture did not assign an order id");
        helper.assertTrue(colony.getWorkManager().getWorkOrder(order.getID()) == order,
          "C2S work-order fixture did not register the order");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S work-order fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, WorkOrderChangeMessage.class);
        helper.assertTrue(messageId > 0, "WorkOrderChange message was not registered");
        final int priorityCommunicationId = 0x574F5050;
        dispatchServerMessage(channel, server, owner, messageId, priorityCommunicationId,
          new WorkOrderChangeMessage(colony.getDimension(), colony.getID(), order.getID(), false, 7));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(colony.getWorkManager().getWorkOrder(order.getID()) == order,
              "WorkOrderChange priority update removed the work order");
            helper.assertTrue(order.getPriority() == 7,
              "WorkOrderChange message did not update the work-order priority");
            helper.assertTrue(channel.getMessageCache().getIfPresent(priorityCommunicationId) == null,
              "WorkOrderChange priority envelope remained in the split-packet cache");
            final int removeCommunicationId = 0x574F524D;
            dispatchServerMessage(channel, server, owner, messageId, removeCommunicationId,
              new WorkOrderChangeMessage(colony.getDimension(), colony.getID(), order.getID(), true, 0));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(colony.getWorkManager().getWorkOrder(order.getID()) == null,
                  "WorkOrderChange remove message did not remove the work order");
                helper.assertTrue(channel.getMessageCache().getIfPresent(removeCommunicationId) == null,
                  "WorkOrderChange remove envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerBuildingSetStyleMessageUpdatesDeconstructedBuilding(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Building Style Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S building-style fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S building-style fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        final IBuilding building = colony.getBuildingManager().addNewBuilding(townHallHut, level);
        helper.assertTrue(building != null, "C2S building-style fixture Town Hall was not registered");
        building.setDeconstructed();

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S building-style fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, BuildingSetStyleMessage.class);
        helper.assertTrue(messageId > 0, "BuildingSetStyle message was not registered");
        final int communicationId = 0x42535459;
        dispatchServerMessage(channel, server, owner, messageId, communicationId,
          new BuildingSetStyleMessage(colony.getDimension(), colony.getID(), townHall, "Urban Savanna"));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue("Urban Savanna".equals(building.getStructurePack()),
              "BuildingSetStyle message did not update the deconstructed building style: "
                + building.getStructurePack());
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "BuildingSetStyle envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerFarmFieldMessagesConfigureField(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeField = new BlockPos(12, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos fieldPos = helper.absolutePos(relativeField);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeField, ModBlocks.blockScarecrow);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Farm Field Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S farm-field fixture colony was not created");
        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S farm-field fixture has no running server");

        final NetworkChannel channel = Network.getNetwork();
        final int registrationId = findMessageId(channel, FarmFieldRegistrationMessage.class);
        final int seedId = findMessageId(channel, FarmFieldUpdateSeedMessage.class);
        final int resizeId = findMessageId(channel, FarmFieldPlotResizeMessage.class);
        helper.assertTrue(registrationId > 0, "FarmFieldRegistration message was not registered");
        helper.assertTrue(seedId > 0, "FarmFieldUpdateSeed message was not registered");
        helper.assertTrue(resizeId > 0, "FarmFieldPlotResize message was not registered");

        final int registrationCommunicationId = 0x46465247;
        final int seedCommunicationId = 0x46465344;
        final int resizeCommunicationId = 0x4646525A;
        dispatchServerMessage(channel, server, owner, registrationId, registrationCommunicationId,
          new FarmFieldRegistrationMessage(colony.getDimension(), colony.getID(), fieldPos));
        dispatchServerMessage(channel, server, owner, seedId, seedCommunicationId,
          new FarmFieldUpdateSeedMessage(colony.getDimension(), colony.getID(), new ItemStack(Items.CARROT), fieldPos));
        dispatchServerMessage(channel, server, owner, resizeId, resizeCommunicationId,
          new FarmFieldPlotResizeMessage(colony.getDimension(), colony.getID(), 3, Direction.EAST, fieldPos));

        helper.runAfterDelay(1, () ->
        {
            final FarmField field = colony.getBuildingManager().getField(candidate -> candidate.getPosition().equals(fieldPos))
              .map(candidate -> (FarmField) candidate)
              .orElse(null);
            helper.assertTrue(field != null, "FarmFieldRegistration message did not create the field");
            helper.assertTrue(field.getSeed().is(Items.CARROT),
              "FarmFieldUpdateSeed message did not update the seed: " + field.getSeed());
            helper.assertTrue(field.getRadius(Direction.EAST) == 3,
              "FarmFieldPlotResize message did not update the east radius: " + field.getRadius(Direction.EAST));
            helper.assertTrue(channel.getMessageCache().getIfPresent(registrationCommunicationId) == null,
              "FarmFieldRegistration envelope remained in the split-packet cache");
            helper.assertTrue(channel.getMessageCache().getIfPresent(seedCommunicationId) == null,
              "FarmFieldUpdateSeed envelope remained in the split-packet cache");
            helper.assertTrue(channel.getMessageCache().getIfPresent(resizeCommunicationId) == null,
              "FarmFieldPlotResize envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerFarmerFieldMessagesControlAssignment(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeFarmer = new BlockPos(10, 1, 2);
        final BlockPos relativeField = new BlockPos(15, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos farmerPos = helper.absolutePos(relativeFarmer);
        final BlockPos fieldPos = helper.absolutePos(relativeField);
        for (int x = 0; x <= 18; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeFarmer, ModBlocks.blockHutFarmer);
        helper.setBlock(relativeField, ModBlocks.blockScarecrow);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Farmer Fields Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S farmer-fields fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S farmer-fields fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        final BlockEntity farmerEntity = level.getBlockEntity(farmerPos);
        helper.assertTrue(farmerEntity instanceof TileEntityColonyBuilding,
          "C2S farmer-fields fixture did not create a Farmer block entity");
        final TileEntityColonyBuilding farmerHut = (TileEntityColonyBuilding) farmerEntity;
        farmerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        farmerHut.setBlueprintPath("agriculture/horticulture/farm1.blueprint");
        farmerHut.setSchematicName("farm1");
        final IBuilding farmerBuilding = colony.getBuildingManager().addNewBuilding(farmerHut, level);
        helper.assertTrue(farmerBuilding instanceof BuildingFarmer,
          "C2S farmer-fields fixture registered the wrong building: " + farmerBuilding);
        final BuildingFarmer farmer = (BuildingFarmer) farmerBuilding;
        helper.assertTrue(farmer.getBuildingLevel() >= 1,
          "C2S farmer-fields fixture did not resolve its level-one blueprint");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final FarmField field = FarmField.create(fieldPos);
        field.setSeed(new ItemStack(Items.WHEAT));
        helper.assertTrue(field.isValidPlacement(colony),
          "C2S farmer-fields fixture field does not have a valid scarecrow placement");
        helper.assertTrue(colony.getBuildingManager().addField(field),
          "C2S farmer-fields fixture field was not registered in the colony");
        final BuildingFarmer.FarmerFieldsModule fields = farmer.getFirstModuleOccurance(
          BuildingFarmer.FarmerFieldsModule.class);
        helper.assertTrue(fields != null, "C2S farmer-fields fixture did not register its fields module");
        final int moduleId = BuildingModules.FARMER_FIELDS.getRuntimeID();
        helper.assertTrue(farmer.getModule(moduleId) == fields,
          "C2S farmer-fields fixture runtime module id did not resolve its fields module");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S farmer-fields fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int modeId = findMessageId(channel, AssignmentModeMessage.class);
        final int assignId = findMessageId(channel, AssignFieldMessage.class);
        helper.assertTrue(modeId > 0, "AssignmentMode message was not registered");
        helper.assertTrue(assignId > 0, "AssignField message was not registered");

        final int modeCommunicationId = 0x46414D4D;
        final int assignCommunicationId = 0x46415347;
        final int freeCommunicationId = 0x46414652;
        dispatchServerMessage(channel, server, owner, modeId, modeCommunicationId,
          new AssignmentModeMessage(colony.getDimension(), colony.getID(), farmerPos, true, moduleId));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(fields.assignManually(),
              "AssignmentMode message did not enable manual Farmer field assignment");
            helper.assertTrue(channel.getMessageCache().getIfPresent(modeCommunicationId) == null,
              "AssignmentMode envelope remained in the split-packet cache");
            dispatchServerMessage(channel, server, owner, assignId, assignCommunicationId,
              new AssignFieldMessage(colony.getDimension(), colony.getID(), farmerPos, field, true, moduleId));

            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(fields.getOwnedFields().contains(field),
                  "AssignField message did not assign the FarmField to the Farmer");
                helper.assertTrue(farmer.getID().equals(field.getBuildingId()),
                  "AssignField message stored the wrong Farmer owner: " + field.getBuildingId());
                helper.assertTrue(channel.getMessageCache().getIfPresent(assignCommunicationId) == null,
                  "AssignField envelope remained in the split-packet cache");
                dispatchServerMessage(channel, server, owner, assignId, freeCommunicationId,
                  new AssignFieldMessage(colony.getDimension(), colony.getID(), farmerPos, field, false, moduleId));

                helper.runAfterDelay(1, () ->
                {
                    helper.assertTrue(fields.getOwnedFields().isEmpty() && !field.isTaken(),
                      "AssignField message did not free the FarmField");
                    helper.assertTrue(channel.getMessageCache().getIfPresent(freeCommunicationId) == null,
                      "AssignField free envelope remained in the split-packet cache");
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerTriggerSettingMessageUpdatesFarmer(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeFarmer = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos farmerPos = helper.absolutePos(relativeFarmer);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeFarmer, ModBlocks.blockHutFarmer);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Farmer Settings Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S farmer-settings fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S farmer-settings fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        colony.getBuildingManager().addNewBuilding(townHallHut, level);

        final BlockEntity farmerEntity = level.getBlockEntity(farmerPos);
        helper.assertTrue(farmerEntity instanceof TileEntityColonyBuilding,
          "C2S farmer-settings fixture did not create a Farmer block entity");
        final TileEntityColonyBuilding farmerHut = (TileEntityColonyBuilding) farmerEntity;
        farmerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        farmerHut.setBlueprintPath("agriculture/horticulture/farm1.blueprint");
        farmerHut.setSchematicName("farm1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(farmerHut, level);
        helper.assertTrue(registered instanceof BuildingFarmer,
          "C2S farmer-settings fixture registered the wrong building: " + registered);
        final BuildingFarmer farmer = (BuildingFarmer) registered;
        helper.assertTrue(farmer.requestFertilizer(),
          "C2S farmer-settings fixture did not start with fertilizer requests enabled");
        final int settingsModuleId = BuildingModules.FARMER_SETTINGS.getRuntimeID();
        helper.assertTrue(farmer.getModule(settingsModuleId) != null,
          "C2S farmer-settings fixture runtime module id did not resolve its settings module");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S farmer-settings fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, TriggerSettingMessage.class);
        helper.assertTrue(messageId > 0, "TriggerSetting message was not registered");
        final int communicationId = 0x46525447;
        dispatchServerMessage(channel, server, owner, messageId, communicationId,
          new TriggerSettingMessage(colony.getDimension(), colony.getID(), farmerPos,
            BuildingFarmer.FERTILIZE, new BoolSetting(false), settingsModuleId));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(!farmer.requestFertilizer(),
              "TriggerSetting message did not disable Farmer fertilizer requests");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "TriggerSetting envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 240)
    public void clientToServerReactivateBuildingMessageRestoresDeactivatedHut(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeBuilder = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos builderPos = helper.absolutePos(relativeBuilder);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeBuilder, ModBlocks.blockHutBuilder);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Reactivate Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S reactivate fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S reactivate fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S reactivate fixture Town Hall was not registered");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final BlockEntity builderEntity = level.getBlockEntity(builderPos);
        helper.assertTrue(builderEntity instanceof TileEntityColonyBuilding,
          "C2S reactivate fixture did not create a Builder block entity");
        final TileEntityColonyBuilding builderHut = (TileEntityColonyBuilding) builderEntity;
        builderHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        builderHut.setBlueprintPath("fundamentals/builder1.blueprint");
        builderHut.setSchematicName("builder1");
        final Map<BlockPos, java.util.List<String>> tags = new java.util.HashMap<>();
        tags.put(BlockPos.ZERO, new java.util.ArrayList<>(java.util.List.of(Constants.DEFAULT_STYLE, "deactivated")));
        builderHut.setPositionedTags(tags);
        helper.assertTrue(colony.getBuildingManager().getBuilding(builderPos) == null,
          "C2S reactivate fixture hut was unexpectedly registered before the message");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S reactivate fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, ReactivateBuildingMessage.class);
        helper.assertTrue(messageId > 0, "ReactivateBuilding message was not registered");
        final int communicationId = 0x52454143;
        dispatchServerMessage(channel, server, owner, messageId, communicationId,
          new ReactivateBuildingMessage(builderPos));

        helper.runAfterDelay(160, () ->
        {
            final IBuilding restored = colony.getBuildingManager().getBuilding(builderPos);
            helper.assertTrue(restored instanceof BuildingBuilder,
              "ReactivateBuilding message did not register the Builder: " + restored);
            helper.assertTrue(!builderHut.getPositionedTags().getOrDefault(BlockPos.ZERO, java.util.List.of())
                .contains("deactivated"),
              "ReactivateBuilding message left the hut marked as deactivated");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "ReactivateBuilding envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerAssignFilterableEntityMessageUpdatesGuardList(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeGuardTower = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos guardTowerPos = helper.absolutePos(relativeGuardTower);
        for (int x = 0; x <= 16; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeGuardTower, ModBlocks.blockHutGuardTower);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Guard Entity Filter Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S guard-entity-filter fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S guard-entity-filter fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S guard-entity-filter fixture Town Hall was not registered");

        final BlockEntity guardTowerEntity = level.getBlockEntity(guardTowerPos);
        helper.assertTrue(guardTowerEntity instanceof TileEntityColonyBuilding,
          "C2S guard-entity-filter fixture did not create a guard-tower block entity");
        final TileEntityColonyBuilding guardTowerHut = (TileEntityColonyBuilding) guardTowerEntity;
        guardTowerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        guardTowerHut.setBlueprintPath("military/guardtower1.blueprint");
        guardTowerHut.setSchematicName("guardtower1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(guardTowerHut, level);
        helper.assertTrue(registered instanceof BuildingGuardTower,
          "C2S guard-entity-filter fixture registered the wrong building: " + registered);
        final EntityListModule entityList = registered.getModule(BuildingModules.GUARD_ENTITY_LIST);
        helper.assertTrue(entityList != null,
          "C2S guard-entity-filter fixture did not register the guard entity list module");
        final ResourceLocation entity = new ResourceLocation("minecraft", "zombie");
        helper.assertTrue(!entityList.isEntityInList(entity),
          "C2S guard-entity-filter fixture entity list was not initially empty");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S guard-entity-filter fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, AssignFilterableEntityMessage.class);
        helper.assertTrue(messageId > 0, "AssignFilterableEntity message was not registered");
        final int assignCommunicationId = 0x45464C41;
        dispatchServerMessage(channel, server, owner, messageId, assignCommunicationId,
          new AssignFilterableEntityMessage(colony.getDimension(), colony.getID(), guardTowerPos,
            BuildingModules.GUARD_ENTITY_LIST.getRuntimeID(), entity, true));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(entityList.isEntityInList(entity),
              "AssignFilterableEntity message did not add the guard entity filter");
            helper.assertTrue(channel.getMessageCache().getIfPresent(assignCommunicationId) == null,
              "AssignFilterableEntity add envelope remained in the split-packet cache");
            final int removeCommunicationId = 0x45464C52;
            dispatchServerMessage(channel, server, owner, messageId, removeCommunicationId,
              new AssignFilterableEntityMessage(colony.getDimension(), colony.getID(), guardTowerPos,
                BuildingModules.GUARD_ENTITY_LIST.getRuntimeID(), entity, false));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(!entityList.isEntityInList(entity),
                  "AssignFilterableEntity message did not remove the guard entity filter");
                helper.assertTrue(channel.getMessageCache().getIfPresent(removeCommunicationId) == null,
                  "AssignFilterableEntity remove envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerMinerSetLevelMessageUpdatesMiner(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeMiner = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos minerPos = helper.absolutePos(relativeMiner);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeMiner, ModBlocks.blockHutMiner);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Miner Level Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S miner-level fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S miner-level fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S miner-level fixture Town Hall was not registered");

        final BlockEntity minerEntity = level.getBlockEntity(minerPos);
        helper.assertTrue(minerEntity instanceof TileEntityColonyBuilding,
          "C2S miner-level fixture did not create a Miner block entity");
        final TileEntityColonyBuilding minerHut = (TileEntityColonyBuilding) minerEntity;
        minerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        minerHut.setBlueprintPath("fundamentals/mine1.blueprint");
        minerHut.setSchematicName("mine1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(minerHut, level);
        helper.assertTrue(registered instanceof BuildingMiner,
          "C2S miner-level fixture registered the wrong building: " + registered);
        final BuildingMiner miner = (BuildingMiner) registered;
        final MinerLevelManagementModule levels = miner.getModule(BuildingModules.MINER_LEVELS);
        helper.assertTrue(levels != null, "C2S miner-level fixture did not register the level module");
        final CompoundTag before = new CompoundTag();
        levels.serializeNBT(before);
        helper.assertTrue(before.getInt("currentLevel") == 0,
          "C2S miner-level fixture did not start at level zero");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S miner-level fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, MinerSetLevelMessage.class);
        helper.assertTrue(messageId > 0, "MinerSetLevel message was not registered");
        final int communicationId = 0x4D4C564C;
        dispatchServerMessage(channel, server, owner, messageId, communicationId,
          new MinerSetLevelMessage(colony.getDimension(), colony.getID(), minerPos, 2));

        helper.runAfterDelay(1, () ->
        {
            final CompoundTag after = new CompoundTag();
            levels.serializeNBT(after);
            helper.assertTrue(after.getInt("currentLevel") == 2,
              "MinerSetLevel message did not persist the selected level");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "MinerSetLevel envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 240)
    public void clientToServerMinerRepairLevelMessageCreatesWorkOrder(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeMiner = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos minerPos = helper.absolutePos(relativeMiner);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeMiner, ModBlocks.blockHutMiner);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Miner Repair Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S miner-repair fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S miner-repair fixture Town Hall did not create a block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S miner-repair fixture Town Hall was not registered");

        final BlockEntity minerEntity = level.getBlockEntity(minerPos);
        helper.assertTrue(minerEntity instanceof TileEntityColonyBuilding,
          "C2S miner-repair fixture did not create a Miner block entity");
        final TileEntityColonyBuilding minerHut = (TileEntityColonyBuilding) minerEntity;
        minerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        minerHut.setBlueprintPath("fundamentals/mine1.blueprint");
        minerHut.setSchematicName("mine1");
        final Map<BlockPos, java.util.List<String>> tags = new java.util.HashMap<>();
        tags.put(BlockPos.ZERO, java.util.List.of("cobble"));
        tags.put(new BlockPos(1, 0, 0), java.util.List.of("ladder"));
        minerHut.setPositionedTags(tags);
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(minerHut, level);
        helper.assertTrue(registered instanceof BuildingMiner,
          "C2S miner-repair fixture registered the wrong building: " + registered);
        final BuildingMiner miner = (BuildingMiner) registered;
        final MinerLevelManagementModule levels = miner.getModule(BuildingModules.MINER_LEVELS);
        helper.assertTrue(levels != null, "C2S miner-repair fixture did not register the level module");
        levels.addLevel(new MinerLevel(miner, level.getMinBuildHeight() + 10, null));
        helper.assertTrue(levels.getNumberOfLevels() == 1,
          "C2S miner-repair fixture could not create a repairable mine level");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final int workOrdersBefore = colony.getWorkManager().getOrderedList(WorkOrderMiner.class, minerPos).size();
        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S miner-repair fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, MinerRepairLevelMessage.class);
        helper.assertTrue(messageId > 0, "MinerRepairLevel message was not registered");
        final int communicationId = 0x4D52504C;
        dispatchServerMessage(channel, server, owner, messageId, communicationId,
          new MinerRepairLevelMessage(colony.getDimension(), colony.getID(), minerPos, 0));

        helper.runAfterDelay(1, () ->
        {
            final java.util.List<WorkOrderMiner> workOrders =
              colony.getWorkManager().getOrderedList(WorkOrderMiner.class, minerPos);
            helper.assertTrue(workOrders.size() == workOrdersBefore + 1,
              "MinerRepairLevel message did not create a shaft repair work order");
            helper.assertTrue(workOrders.stream().anyMatch(order -> minerPos.equals(order.getMinerBuilding())),
              "MinerRepairLevel repair order was not associated with the Miner");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "MinerRepairLevel envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerGuardSetMinePosMessageUpdatesPatrolMine(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeMiner = new BlockPos(10, 1, 2);
        final BlockPos relativeGuardTower = new BlockPos(14, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos minerPos = helper.absolutePos(relativeMiner);
        final BlockPos guardTowerPos = helper.absolutePos(relativeGuardTower);
        for (int x = 0; x <= 16; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeMiner, ModBlocks.blockHutMiner);
        helper.setBlock(relativeGuardTower, ModBlocks.blockHutGuardTower);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Guard Mine Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S guard-mine fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S guard-mine fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S guard-mine fixture Town Hall was not registered");

        final BlockEntity minerEntity = level.getBlockEntity(minerPos);
        helper.assertTrue(minerEntity instanceof TileEntityColonyBuilding,
          "C2S guard-mine fixture did not create a Miner block entity");
        final TileEntityColonyBuilding minerHut = (TileEntityColonyBuilding) minerEntity;
        minerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        minerHut.setBlueprintPath("fundamentals/mine1.blueprint");
        minerHut.setSchematicName("mine1");
        final IBuilding registeredMiner = colony.getBuildingManager().addNewBuilding(minerHut, level);
        helper.assertTrue(registeredMiner instanceof BuildingMiner,
          "C2S guard-mine fixture registered the wrong miner: " + registeredMiner);

        final BlockEntity guardTowerEntity = level.getBlockEntity(guardTowerPos);
        helper.assertTrue(guardTowerEntity instanceof TileEntityColonyBuilding,
          "C2S guard-mine fixture did not create a Guard Tower block entity");
        final TileEntityColonyBuilding guardTowerHut = (TileEntityColonyBuilding) guardTowerEntity;
        guardTowerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        guardTowerHut.setBlueprintPath("military/guardtower1.blueprint");
        guardTowerHut.setSchematicName("guardtower1");
        final IBuilding registeredGuard = colony.getBuildingManager().addNewBuilding(guardTowerHut, level);
        helper.assertTrue(registeredGuard instanceof BuildingGuardTower,
          "C2S guard-mine fixture registered the wrong guard tower: " + registeredGuard);
        final BuildingGuardTower guardTower = (BuildingGuardTower) registeredGuard;
        helper.assertTrue(guardTower.getMinePos() == null,
          "C2S guard-mine fixture unexpectedly started with an assigned mine");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S guard-mine fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, GuardSetMinePosMessage.class);
        helper.assertTrue(messageId > 0, "GuardSetMinePos message was not registered");
        final int setCommunicationId = 0x474D5345;
        dispatchServerMessage(channel, server, owner, messageId, setCommunicationId,
          new GuardSetMinePosMessage(colony.getDimension(), colony.getID(), guardTowerPos, minerPos));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(minerPos.equals(guardTower.getMinePos()),
              "GuardSetMinePos message did not assign the Miner as patrol target");
            helper.assertTrue(channel.getMessageCache().getIfPresent(setCommunicationId) == null,
              "GuardSetMinePos assignment envelope remained in the split-packet cache");
            final int clearCommunicationId = 0x474D434C;
            dispatchServerMessage(channel, server, owner, messageId, clearCommunicationId,
              new GuardSetMinePosMessage(colony.getDimension(), colony.getID(), guardTowerPos));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(guardTower.getMinePos() == null,
                  "GuardSetMinePos clear message did not remove the patrol target");
                helper.assertTrue(channel.getMessageCache().getIfPresent(clearCommunicationId) == null,
                  "GuardSetMinePos clear envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 160)
    public void clientToServerMarkBuildingDirtyMessageMarksTownHall(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Dirty Building Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S dirty-building fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S dirty-building fixture did not create a Town Hall block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(townHallHut, level);
        helper.assertTrue(registered != null, "C2S dirty-building fixture Town Hall was not registered");
        registered.clearDirty();
        helper.assertTrue(!registered.isDirty(), "C2S dirty-building fixture could not clear initial dirty state");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S dirty-building fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, MarkBuildingDirtyMessage.class);
        helper.assertTrue(messageId > 0, "MarkBuildingDirty message was not registered");
        final int communicationId = 0x44495254;
        dispatchServerMessage(channel, server, owner, messageId, communicationId,
          new MarkBuildingDirtyMessage(colony.getDimension(), colony.getID(), townHall));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(registered.isDirty(),
              "MarkBuildingDirty message did not mark the Town Hall dirty");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "MarkBuildingDirty envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 160)
    public void clientToServerToggleMoveInMessageUpdatesColony(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Move In Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S move-in fixture colony was not created");
        helper.assertTrue(colony.canMoveIn(), "C2S move-in fixture did not start enabled");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S move-in fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, ToggleMoveInMessage.class);
        helper.assertTrue(messageId > 0, "ToggleMoveIn message was not registered");
        final int disableCommunicationId = 0x4D4F4646;
        dispatchServerMessage(channel, server, owner, messageId, disableCommunicationId,
          new ToggleMoveInMessage(colony.getDimension(), colony.getID(), false));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(!colony.canMoveIn(), "ToggleMoveIn message did not disable move-in");
            helper.assertTrue(channel.getMessageCache().getIfPresent(disableCommunicationId) == null,
              "ToggleMoveIn disable envelope remained in the split-packet cache");
            final int enableCommunicationId = 0x4D4F4E;
            dispatchServerMessage(channel, server, owner, messageId, enableCommunicationId,
              new ToggleMoveInMessage(colony.getDimension(), colony.getID(), true));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(colony.canMoveIn(), "ToggleMoveIn message did not re-enable move-in");
                helper.assertTrue(channel.getMessageCache().getIfPresent(enableCommunicationId) == null,
                  "ToggleMoveIn enable envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerPauseCitizenMessageTogglesCitizenPauseState(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Pause Citizen Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S pause-citizen fixture colony was not created");
        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S pause-citizen fixture did not create a Town Hall block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S pause-citizen fixture Town Hall was not registered");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);
        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, townHall.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "C2S pause-citizen fixture could not create a live citizen");
        helper.assertTrue(!citizen.isPaused(), "C2S pause-citizen fixture started paused");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S pause-citizen fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, PauseCitizenMessage.class);
        helper.assertTrue(messageId > 0, "PauseCitizen message was not registered");
        final int pauseCommunicationId = 0x50415553;
        dispatchServerMessage(channel, server, owner, messageId, pauseCommunicationId,
          new PauseCitizenMessage(colony.getDimension(), colony.getID(), citizen.getId()));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(citizen.isPaused(), "PauseCitizen message did not pause the citizen");
            helper.assertTrue(channel.getMessageCache().getIfPresent(pauseCommunicationId) == null,
              "PauseCitizen pause envelope remained in the split-packet cache");
            final int resumeCommunicationId = 0x50415552;
            dispatchServerMessage(channel, server, owner, messageId, resumeCommunicationId,
              new PauseCitizenMessage(colony.getDimension(), colony.getID(), citizen.getId()));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(!citizen.isPaused(), "PauseCitizen message did not resume the citizen");
                helper.assertTrue(channel.getMessageCache().getIfPresent(resumeCommunicationId) == null,
                  "PauseCitizen resume envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 240)
    public void clientToServerHireFireMessageUpdatesFarmerWorker(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeFarmer = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos farmerPos = helper.absolutePos(relativeFarmer);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeFarmer, ModBlocks.blockHutFarmer);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Hire Fire Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S hire-fire fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S hire-fire fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S hire-fire fixture Town Hall was not registered");

        final BlockEntity farmerEntity = level.getBlockEntity(farmerPos);
        helper.assertTrue(farmerEntity instanceof TileEntityColonyBuilding,
          "C2S hire-fire fixture did not create a Farmer block entity");
        final TileEntityColonyBuilding farmerHut = (TileEntityColonyBuilding) farmerEntity;
        farmerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        farmerHut.setBlueprintPath("agriculture/horticulture/farm1.blueprint");
        farmerHut.setSchematicName("farm1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(farmerHut, level);
        helper.assertTrue(registered instanceof BuildingFarmer,
          "C2S hire-fire fixture registered the wrong building: " + registered);
        final BuildingFarmer farmer = (BuildingFarmer) registered;
        final WorkerBuildingModule workerModule = farmer.getModuleMatching(
          WorkerBuildingModule.class, module -> module.getJobEntry() == ModJobs.farmer.get());
        helper.assertTrue(workerModule != null, "C2S hire-fire Farmer worker module was not registered");
        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, farmerPos.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "C2S hire-fire fixture could not create a live citizen");
        helper.assertTrue(!workerModule.hasAssignedCitizen(citizen),
          "C2S hire-fire fixture citizen was assigned before the message");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S hire-fire fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, HireFireMessage.class);
        helper.assertTrue(messageId > 0, "HireFire message was not registered");
        final int hireCommunicationId = 0x48495245;
        dispatchServerMessage(channel, server, owner, messageId, hireCommunicationId,
          new HireFireMessage(colony.getDimension(), colony.getID(), farmerPos, true, citizen.getId(),
            BuildingModules.FARMER_CRAFT.getRuntimeID()));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(workerModule.hasAssignedCitizen(citizen),
              "HireFire message did not assign the Farmer citizen");
            helper.assertTrue(citizen.getJob() instanceof JobFarmer,
              "HireFire message did not create the Farmer job");
            helper.assertTrue(channel.getMessageCache().getIfPresent(hireCommunicationId) == null,
              "HireFire hire envelope remained in the split-packet cache");
            final int fireCommunicationId = 0x46495245;
            dispatchServerMessage(channel, server, owner, messageId, fireCommunicationId,
              new HireFireMessage(colony.getDimension(), colony.getID(), farmerPos, false, citizen.getId(),
                BuildingModules.FARMER_CRAFT.getRuntimeID()));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(!workerModule.hasAssignedCitizen(citizen),
                  "HireFire message did not remove the Farmer citizen");
                helper.assertTrue(channel.getMessageCache().getIfPresent(fireCommunicationId) == null,
                  "HireFire fire envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerBuildingHiringModeMessageUpdatesFarmer(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeFarmer = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos farmerPos = helper.absolutePos(relativeFarmer);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeFarmer, ModBlocks.blockHutFarmer);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Hiring Mode Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S hiring-mode fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S hiring-mode fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S hiring-mode fixture Town Hall was not registered");

        final BlockEntity farmerEntity = level.getBlockEntity(farmerPos);
        helper.assertTrue(farmerEntity instanceof TileEntityColonyBuilding,
          "C2S hiring-mode fixture did not create a Farmer block entity");
        final TileEntityColonyBuilding farmerHut = (TileEntityColonyBuilding) farmerEntity;
        farmerHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        farmerHut.setBlueprintPath("agriculture/horticulture/farm1.blueprint");
        farmerHut.setSchematicName("farm1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(farmerHut, level);
        helper.assertTrue(registered instanceof BuildingFarmer,
          "C2S hiring-mode fixture registered the wrong building: " + registered);
        final BuildingFarmer farmer = (BuildingFarmer) registered;
        final WorkerBuildingModule workerModule = farmer.getModuleMatching(
          WorkerBuildingModule.class, module -> module.getJobEntry() == ModJobs.farmer.get());
        helper.assertTrue(workerModule != null, "C2S hiring-mode Farmer worker module was not registered");
        helper.assertTrue(workerModule.getHiringMode() == HiringMode.DEFAULT,
          "C2S hiring-mode fixture did not start in DEFAULT mode");

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S hiring-mode fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, BuildingHiringModeMessage.class);
        helper.assertTrue(messageId > 0, "BuildingHiringMode message was not registered");
        final int autoCommunicationId = 0x484D4155;
        dispatchServerMessage(channel, server, owner, messageId, autoCommunicationId,
          new BuildingHiringModeMessage(colony.getDimension(), colony.getID(), farmerPos,
            HiringMode.AUTO, BuildingModules.FARMER_CRAFT.getRuntimeID()));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(workerModule.getHiringMode() == HiringMode.AUTO,
              "BuildingHiringMode message did not enable AUTO mode");
            helper.assertTrue(channel.getMessageCache().getIfPresent(autoCommunicationId) == null,
              "BuildingHiringMode AUTO envelope remained in the split-packet cache");
            final int defaultCommunicationId = 0x484D4445;
            dispatchServerMessage(channel, server, owner, messageId, defaultCommunicationId,
              new BuildingHiringModeMessage(colony.getDimension(), colony.getID(), farmerPos,
                HiringMode.DEFAULT, BuildingModules.FARMER_CRAFT.getRuntimeID()));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(workerModule.getHiringMode() == HiringMode.DEFAULT,
                  "BuildingHiringMode message did not restore DEFAULT mode");
                helper.assertTrue(channel.getMessageCache().getIfPresent(defaultCommunicationId) == null,
                  "BuildingHiringMode DEFAULT envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerCourierHiringModeMessageUpdatesWarehouse(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeWarehouse = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos warehousePos = helper.absolutePos(relativeWarehouse);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeWarehouse, ModBlocks.blockHutWareHouse);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Courier Hiring Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S courier-hiring fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S courier-hiring fixture Town Hall did not create a block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S courier-hiring fixture Town Hall was not registered");

        final BlockEntity warehouseEntity = level.getBlockEntity(warehousePos);
        helper.assertTrue(warehouseEntity instanceof TileEntityColonyBuilding,
          "C2S courier-hiring fixture did not create a warehouse block entity");
        final TileEntityColonyBuilding warehouseHut = (TileEntityColonyBuilding) warehouseEntity;
        warehouseHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        warehouseHut.setBlueprintPath("craftsmanship/storage/warehouse1.blueprint");
        warehouseHut.setSchematicName("warehouse1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(warehouseHut, level);
        helper.assertTrue(registered instanceof BuildingWareHouse,
          "C2S courier-hiring fixture registered the wrong building: " + registered);
        final BuildingWareHouse warehouse = (BuildingWareHouse) registered;
        final CourierAssignmentModule courierModule = warehouse.getFirstModuleOccurance(CourierAssignmentModule.class);
        helper.assertTrue(courierModule != null,
          "C2S courier-hiring fixture did not register the warehouse courier module");
        helper.assertTrue(courierModule.getHiringMode() == HiringMode.DEFAULT,
          "C2S courier-hiring fixture did not start in DEFAULT mode");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S courier-hiring fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, CourierHiringModeMessage.class);
        helper.assertTrue(messageId > 0, "CourierHiringMode message was not registered");
        final int autoCommunicationId = 0x43484155;
        dispatchServerMessage(channel, server, owner, messageId, autoCommunicationId,
          new CourierHiringModeMessage(colony.getDimension(), colony.getID(), warehousePos,
            HiringMode.AUTO, BuildingModules.WAREHOUSE_COURIERS.getRuntimeID()));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(courierModule.getHiringMode() == HiringMode.AUTO,
              "CourierHiringMode message did not enable AUTO mode");
            helper.assertTrue(channel.getMessageCache().getIfPresent(autoCommunicationId) == null,
              "CourierHiringMode AUTO envelope remained in the split-packet cache");
            final int defaultCommunicationId = 0x43484144;
            dispatchServerMessage(channel, server, owner, messageId, defaultCommunicationId,
              new CourierHiringModeMessage(colony.getDimension(), colony.getID(), warehousePos,
                HiringMode.DEFAULT, BuildingModules.WAREHOUSE_COURIERS.getRuntimeID()));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(courierModule.getHiringMode() == HiringMode.DEFAULT,
                  "CourierHiringMode message did not restore DEFAULT mode");
                helper.assertTrue(channel.getMessageCache().getIfPresent(defaultCommunicationId) == null,
                  "CourierHiringMode DEFAULT envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerQuarryHiringModeMessageUpdatesSimpleQuarry(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeMiner = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos minerPos = helper.absolutePos(relativeMiner);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeMiner, ModBlocks.blockSimpleQuarry);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Quarry Hiring Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S quarry-hiring fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S quarry-hiring fixture Town Hall did not create a block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S quarry-hiring fixture Town Hall was not registered");

        final BlockEntity quarryEntity = level.getBlockEntity(minerPos);
        helper.assertTrue(quarryEntity instanceof TileEntityColonyBuilding,
          "C2S quarry-hiring fixture did not create a Simple Quarry block entity");
        final TileEntityColonyBuilding quarryHut = (TileEntityColonyBuilding) quarryEntity;
        helper.assertTrue(StructurePacks.hasPack("Space Wars"),
          "C2S quarry-hiring fixture did not discover the Space Wars structure pack");
        quarryHut.setStructurePack(StructurePacks.getStructurePack("Space Wars"));
        quarryHut.setBlueprintPath("infrastructure/mineshafts/simplequarry1.blueprint");
        quarryHut.setSchematicName("simplequarry1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(quarryHut, level);
        helper.assertTrue(registered != null,
          "C2S quarry-hiring fixture could not register the Simple Quarry");
        final QuarryModule quarryModule = registered.getModule(BuildingModules.SIMPLE_QUARRY);
        helper.assertTrue(quarryModule != null,
          "C2S quarry-hiring fixture did not register the quarry module");
        helper.assertTrue(quarryModule.getHiringMode() == HiringMode.DEFAULT,
          "C2S quarry-hiring fixture did not start in DEFAULT mode");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S quarry-hiring fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, QuarryHiringModeMessage.class);
        helper.assertTrue(messageId > 0, "QuarryHiringMode message was not registered");
        final int autoCommunicationId = 0x51484155;
        dispatchServerMessage(channel, server, owner, messageId, autoCommunicationId,
          new QuarryHiringModeMessage(colony.getDimension(), colony.getID(), minerPos,
            HiringMode.AUTO, BuildingModules.SIMPLE_QUARRY.getRuntimeID()));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(quarryModule.getHiringMode() == HiringMode.AUTO,
              "QuarryHiringMode message did not enable AUTO mode");
            helper.assertTrue(channel.getMessageCache().getIfPresent(autoCommunicationId) == null,
              "QuarryHiringMode AUTO envelope remained in the split-packet cache");
            final int defaultCommunicationId = 0x51484144;
            dispatchServerMessage(channel, server, owner, messageId, defaultCommunicationId,
              new QuarryHiringModeMessage(colony.getDimension(), colony.getID(), minerPos,
                HiringMode.DEFAULT, BuildingModules.SIMPLE_QUARRY.getRuntimeID()));
            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(quarryModule.getHiringMode() == HiringMode.DEFAULT,
                  "QuarryHiringMode message did not restore DEFAULT mode");
                helper.assertTrue(channel.getMessageCache().getIfPresent(defaultCommunicationId) == null,
                  "QuarryHiringMode DEFAULT envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerAdjustSkillMessageUpdatesCreativeCitizen(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = new ServerPlayer(level.getServer(), level,
          new GameProfile(UUID.randomUUID(), "c2s-skill-player"))
        {
            @Override
            public boolean isSpectator()
            {
                return false;
            }

            @Override
            public boolean isCreative()
            {
                return true;
            }
        };
        level.getServer().getPlayerList().placeNewPlayer(new Connection(PacketFlow.SERVERBOUND), owner);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Skill Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S skill fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S skill fixture did not create a Town Hall block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S skill fixture Town Hall was not registered");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 4, level, true);

        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(null, level, townHall.above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "C2S skill fixture could not create a live citizen");
        final int before = citizen.getCitizenSkillHandler().getLevel(Skill.Strength);

        final MinecraftServer server = level.getServer();
        helper.assertTrue(server != null, "C2S skill fixture has no running server");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, AdjustSkillCitizenMessage.class);
        helper.assertTrue(messageId > 0, "AdjustSkillCitizen message was not registered");
        final int communicationId = 0x534B494C;
        dispatchServerMessage(channel, server, owner, messageId, communicationId,
          new AdjustSkillCitizenMessage(colony.getDimension(), colony.getID(), citizen.getId(), 2, Skill.Strength));

        helper.runAfterDelay(1, () ->
        {
            helper.assertTrue(citizen.getCitizenSkillHandler().getLevel(Skill.Strength) == before + 2,
              "AdjustSkillCitizen message did not increment the Strength skill");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "AdjustSkillCitizen envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerResearchMessageStartsResearch(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        final BlockPos relativeTownHall = new BlockPos(2, 1, 2);
        final BlockPos relativeUniversity = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos universityPos = helper.absolutePos(relativeUniversity);
        for (int x = 0; x <= 12; x++)
        {
            for (int z = 0; z <= 4; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeUniversity, ModBlocks.blockHutUniversity);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        helper.assertTrue(!owner.isCreative(), "C2S research fixture owner remained creative");
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Research Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S research fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S research fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S research fixture Town Hall was not registered");

        final BlockEntity universityEntity = level.getBlockEntity(universityPos);
        helper.assertTrue(universityEntity instanceof TileEntityColonyBuilding,
          "C2S research fixture did not create a university block entity");
        final TileEntityColonyBuilding universityHut = (TileEntityColonyBuilding) universityEntity;
        universityHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        universityHut.setBlueprintPath("education/university1.blueprint");
        universityHut.setSchematicName("university1");
        final IBuilding registered = colony.getBuildingManager().addNewBuilding(universityHut, level);
        helper.assertTrue(registered instanceof BuildingUniversity,
          "C2S research fixture registered the wrong building: " + registered);
        final BuildingUniversity university = (BuildingUniversity) registered;
        helper.assertTrue(university.getBuildingLevel() >= 1,
          "C2S research fixture did not resolve its level-one blueprint");

        final ResourceLocation branch = new ResourceLocation(Constants.MOD_ID, "civilian");
        final ResourceLocation researchId = new ResourceLocation(Constants.MOD_ID, "civilian/ambition");
        final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, researchId);
        helper.assertTrue(research != null && research.canResearch(1, colony.getResearchManager().getResearchTree()),
          "C2S research fixture was not eligible to start");
        owner.getInventory().clearContent();
        owner.getInventory().setItem(0, new ItemStack(Items.DIAMOND));

        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, TryResearchMessage.class);
        helper.assertTrue(messageId > 0, "TryResearch message was not registered");
        final TryResearchMessage original = new TryResearchMessage(
          colony.getDimension(), colony.getID(), university.getID(), researchId, branch, false);
        final int communicationId = 0x43525352;
        final SplitPacketMessage envelope = new SplitPacketMessage(
          communicationId, 0, true, messageId, encode(original));
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.wrappedBuffer(encode(envelope)));
        try
        {
            final MinecraftServer server = level.getServer();
            helper.assertTrue(server != null, "C2S research fixture has no running server");
            channel.getRawChannel().handleServerPacket(server, owner, packet);
        }
        finally
        {
            packet.release();
        }

        helper.runAfterDelay(1, () ->
        {
            final ILocalResearch localResearch = colony.getResearchManager().getResearchTree().getResearch(branch, researchId);
            helper.assertTrue(localResearch != null && localResearch.getState() == ResearchState.IN_PROGRESS,
              "C2S research message did not start the selected research");
            helper.assertTrue(owner.getInventory().countItem(Items.DIAMOND) == 0,
              "C2S research message did not consume the research cost");
            helper.assertTrue(colony.getResearchManager().getResearchTree().getResearchInProgress().size() == 1,
              "C2S research message did not register the selected research as in progress");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "C2S research envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerCreateColonyMessageCreatesColony(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        BlockPos relativeTownHall = null;
        for (int offset = 49152; offset <= 65536 && relativeTownHall == null; offset += 256)
        {
            final BlockPos candidate = new BlockPos(offset, 1, offset);
            if (IColonyManager.getInstance().isFarEnoughFromColonies(level, helper.absolutePos(candidate)))
            {
                relativeTownHall = candidate;
            }
        }
        helper.assertTrue(relativeTownHall != null,
          "C2S colony fixture could not find an unclaimed colony position");
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        for (int x = relativeTownHall.getX() - 2; x <= relativeTownHall.getX() + 2; x++)
        {
            for (int z = relativeTownHall.getZ() - 2; z <= relativeTownHall.getZ() + 2; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S colony fixture Town Hall did not create a building block entity");
        helper.assertTrue(IColonyManager.getInstance().getIColonyByOwner(level, owner) == null,
          "C2S colony fixture owner unexpectedly already owns a colony");
        helper.assertTrue(IColonyManager.getInstance().isFarEnoughFromColonies(level, townHall),
          "C2S colony fixture position is not available for a new colony");

        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, CreateColonyMessage.class);
        helper.assertTrue(messageId > 0, "CreateColony message was not registered");
        final CreateColonyMessage original = new CreateColonyMessage(
          townHall, false, "Fabric C2S Colony", Constants.DEFAULT_STYLE, "fundamentals/townhall1.blueprint");
        final int communicationId = 0x43434F4C;
        final SplitPacketMessage envelope = new SplitPacketMessage(
          communicationId, 0, true, messageId, encode(original));
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.wrappedBuffer(encode(envelope)));
        try
        {
            final MinecraftServer server = level.getServer();
            helper.assertTrue(server != null, "C2S colony fixture has no running server");
            channel.getRawChannel().handleServerPacket(server, owner, packet);
        }
        finally
        {
            packet.release();
        }

        helper.runAfterDelay(1, () ->
        {
            final IColony colony = IColonyManager.getInstance().getIColonyByOwner(level, owner);
            helper.assertTrue(colony != null, "CreateColony message did not create an owner colony");
            helper.assertTrue("Fabric C2S Colony".equals(colony.getName()),
              "CreateColony message used the wrong colony name: " + colony.getName());
            final IBuilding registeredTownHall = colony.getBuildingManager().getBuilding(townHall);
            helper.assertTrue(registeredTownHall != null,
              "CreateColony message did not register the Town Hall building");
            final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) townHallEntity;
            helper.assertTrue(Constants.DEFAULT_STYLE.equals(hut.getPackName()),
              "CreateColony message did not select the requested structure pack");
            helper.assertTrue("fundamentals/townhall1.blueprint".equals(hut.getBlueprintPath()),
              "CreateColony message did not preserve the requested blueprint path");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "CreateColony envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 200)
    public void clientToServerBuildRequestCreatesRepairWorkOrder(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        BlockPos relativeTownHall = null;
        for (int offset = 16384; offset <= 32768 && relativeTownHall == null; offset += 256)
        {
            final BlockPos candidate = new BlockPos(offset, 1, offset);
            if (IColonyManager.getInstance().isFarEnoughFromColonies(level, helper.absolutePos(candidate)))
            {
                relativeTownHall = candidate;
            }
        }
        helper.assertTrue(relativeTownHall != null,
          "C2S Builder fixture could not find an unclaimed colony position");
        final BlockPos relativeBuilder = new BlockPos(10, 1, 2);
        final BlockPos relativeCitizenSpawn = new BlockPos(10, 1, 2);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos builderPos = helper.absolutePos(relativeTownHall.offset(relativeBuilder.getX() - 2, 0, relativeBuilder.getZ() - 2));
        for (int x = relativeCitizenSpawn.getX() - 1; x <= relativeCitizenSpawn.getX() + 1; x++)
        {
            for (int z = relativeCitizenSpawn.getZ() - 1; z <= relativeCitizenSpawn.getZ() + 1; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        for (int x = relativeTownHall.getX() - 2; x <= relativeTownHall.getX() + 10; x++)
        {
            for (int z = relativeTownHall.getZ() - 2; z <= relativeTownHall.getZ() + 2; z++)
            {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        final BlockPos builderRelativePos = relativeTownHall.offset(relativeBuilder.getX() - 2, 0, relativeBuilder.getZ() - 2);
        helper.setBlock(builderRelativePos, ModBlocks.blockHutBuilder);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Builder Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S Builder fixture colony was not created");

        final BlockEntity townHallEntity = level.getBlockEntity(townHall);
        helper.assertTrue(townHallEntity instanceof TileEntityColonyBuilding,
          "C2S Builder fixture Town Hall did not create a building block entity");
        final TileEntityColonyBuilding townHallHut = (TileEntityColonyBuilding) townHallEntity;
        townHallHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        townHallHut.setBlueprintPath("fundamentals/townhall1.blueprint");
        townHallHut.setSchematicName("townhall1");
        helper.assertTrue(colony.getBuildingManager().addNewBuilding(townHallHut, level) != null,
          "C2S Builder fixture Town Hall was not registered");

        final BlockEntity builderEntity = level.getBlockEntity(builderPos);
        helper.assertTrue(builderEntity instanceof TileEntityColonyBuilding,
          "C2S Builder fixture did not create a Builder block entity");
        final TileEntityColonyBuilding builderHut = (TileEntityColonyBuilding) builderEntity;
        builderHut.setStructurePack(StructurePacks.getStructurePack(Constants.DEFAULT_STYLE));
        builderHut.setBlueprintPath("fundamentals/builder1.blueprint");
        builderHut.setSchematicName("builder1");
        final IBuilding registeredBuilder = colony.getBuildingManager().addNewBuilding(builderHut, level);
        helper.assertTrue(registeredBuilder instanceof BuildingBuilder,
          "C2S Builder fixture registered the wrong building implementation: " + registeredBuilder);
        helper.assertTrue(registeredBuilder.getBuildingLevel() >= 1,
          "C2S Builder fixture did not resolve a level-one Builder blueprint");
        final BuildingBuilder builder = (BuildingBuilder) registeredBuilder;
        final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCitizen(
          null, level, helper.absolutePos(relativeCitizenSpawn).above());
        helper.assertTrue(citizen != null && citizen.getEntity().isPresent(),
          "C2S Builder fixture could not create a live builder citizen");
        final WorkerBuildingModule workerModule = builder.getModuleMatching(
          WorkerBuildingModule.class, module -> module.getJobEntry() == ModJobs.builder.get());
        helper.assertTrue(workerModule != null, "C2S Builder worker module was not registered");
        helper.assertTrue(workerModule.assignCitizen(citizen),
          "C2S Builder worker module rejected the citizen assignment");
        helper.assertTrue(citizen.getJob() instanceof JobBuilder,
          "C2S Builder assignment did not create the builder job");
        final JobBuilder job = citizen.getJob(JobBuilder.class);
        helper.assertTrue(job != null && !job.hasWorkOrder(),
          "C2S Builder job unexpectedly started with a work order");
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, townHall, 2, level, true);

        final IBuilding target = colony.getBuildingManager().getBuilding(townHall);
        helper.assertTrue(target != null && !target.hasWorkOrder(),
          "C2S Builder fixture target unexpectedly started with a work order");
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, BuildRequestMessage.class);
        helper.assertTrue(messageId > 0, "BuildRequest message was not registered");
        final BuildRequestMessage original = new BuildRequestMessage(
          colony.getDimension(), colony.getID(), townHall, BuildRequestMessage.Mode.REPAIR, builderPos);
        final int communicationId = 0x4255494C;
        final SplitPacketMessage envelope = new SplitPacketMessage(
          communicationId, 0, true, messageId, encode(original));
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.wrappedBuffer(encode(envelope)));
        try
        {
            final MinecraftServer server = level.getServer();
            helper.assertTrue(server != null, "C2S Builder fixture has no running server");
            channel.getRawChannel().handleServerPacket(server, owner, packet);
        }
        finally
        {
            packet.release();
        }

        helper.runAfterDelay(1, () ->
        {
            final WorkOrderBuilding order = colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuilding.class).stream()
              .filter(candidate -> candidate.getLocation().equals(townHall))
              .findFirst()
              .orElse(null);
            helper.assertTrue(order != null, "BuildRequest message did not create the Town Hall work order");
            helper.assertTrue(order.getWorkOrderType() == WorkOrderType.REPAIR,
              "BuildRequest message created the wrong work-order type: " + order.getWorkOrderType());
            helper.assertTrue(builderPos.equals(order.getClaimedBy()),
              "BuildRequest message did not assign the requested Builder position");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "BuildRequest envelope remained in the split-packet cache");

            final int selectMessageId = findMessageId(channel, BuilderSelectWorkOrderMessage.class);
            helper.assertTrue(selectMessageId > 0, "BuilderSelectWorkOrder message was not registered");
            final BuilderSelectWorkOrderMessage selectOriginal = new BuilderSelectWorkOrderMessage(
              colony.getDimension(), colony.getID(), builderPos, order.getID());
            final int selectCommunicationId = 0x42554953;
            final SplitPacketMessage selectEnvelope = new SplitPacketMessage(
              selectCommunicationId, 0, true, selectMessageId, encode(selectOriginal));
            final FriendlyByteBuf selectPacket = new FriendlyByteBuf(
              Unpooled.wrappedBuffer(encode(selectEnvelope)));
            try
            {
                final MinecraftServer server = level.getServer();
                helper.assertTrue(server != null, "C2S Builder selection fixture has no running server");
                channel.getRawChannel().handleServerPacket(server, owner, selectPacket);
            }
            finally
            {
                selectPacket.release();
            }

            helper.runAfterDelay(1, () ->
            {
                helper.assertTrue(job.hasWorkOrder() && job.getWorkOrder() == order,
                  "BuilderSelectWorkOrder message did not assign the selected work order to the citizen");
                helper.assertTrue(channel.getMessageCache().getIfPresent(selectCommunicationId) == null,
                  "BuilderSelectWorkOrder envelope remained in the split-packet cache");
                helper.succeed();
            });
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 240)
    public void clientToServerDirectPlaceMessagePlacesTownHall(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        BlockPos relativeTownHall = null;
        for (int offset = 32768; offset <= 49152 && relativeTownHall == null; offset += 256)
        {
            final BlockPos candidate = new BlockPos(offset, 1, offset);
            if (IColonyManager.getInstance().isFarEnoughFromColonies(level, helper.absolutePos(candidate)))
            {
                relativeTownHall = candidate;
            }
        }
        helper.assertTrue(relativeTownHall != null,
          "C2S direct-place fixture could not find an unclaimed Town Hall position");
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final ItemStack townHallStack = new ItemStack(ModBlocks.blockHutTownHall.asItem());
        owner.getInventory().add(townHallStack.copy());
        StructurePacks.selectedPack = StructurePacks.getStructurePack(Constants.DEFAULT_STYLE);
        // Keep this no-colony fixture isolated from concurrent colony-creation tests and resident for the async callback.
        final int townHallChunkX = townHall.getX() >> 4;
        final int townHallChunkZ = townHall.getZ() >> 4;
        for (int chunkX = townHallChunkX - 2; chunkX <= townHallChunkX + 2; chunkX++)
        {
            for (int chunkZ = townHallChunkZ - 2; chunkZ <= townHallChunkZ + 2; chunkZ++)
            {
                level.setChunkForced(chunkX, chunkZ, true);
                level.getChunk(chunkX, chunkZ);
            }
        }

        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, DirectPlaceMessage.class);
        helper.assertTrue(messageId > 0, "DirectPlace message was not registered");
        final DirectPlaceMessage original = new DirectPlaceMessage(
          ModBlocks.blockHutTownHall.defaultBlockState(), townHall, townHallStack);
        final int communicationId = 0x44504C43;
        final SplitPacketMessage envelope = new SplitPacketMessage(
          communicationId, 0, true, messageId, encode(original));
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.wrappedBuffer(encode(envelope)));
        try
        {
            final MinecraftServer server = level.getServer();
            helper.assertTrue(server != null, "C2S direct-place fixture has no running server");
            channel.getRawChannel().handleServerPacket(server, owner, packet);
        }
        finally
        {
            packet.release();
        }

        helper.runAfterDelay(160, () ->
        {
            helper.assertTrue(level.getBlockState(townHall).getBlock() == ModBlocks.blockHutTownHall,
              "DirectPlace message did not place the Town Hall block");
            final BlockEntity blockEntity = level.getBlockEntity(townHall);
            helper.assertTrue(blockEntity instanceof TileEntityColonyBuilding,
              "DirectPlace message did not create the Town Hall block entity");
            final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) blockEntity;
            helper.assertTrue(Constants.DEFAULT_STYLE.equals(hut.getPackName()),
              "DirectPlace message did not preserve the selected structure pack");
            helper.assertTrue("fundamentals/townhall1.blueprint".equals(hut.getBlueprintPath()),
              "DirectPlace message did not resolve the Town Hall blueprint path: " + hut.getBlueprintPath());
            helper.assertTrue(owner.getInventory().countItem(ModBlocks.blockHutTownHall.asItem()) == 0,
              "DirectPlace message did not consume the Town Hall item");
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "DirectPlace envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = TEST_BATCH, timeoutTicks = 240)
    public void clientToServerDecorationBuildRequestCreatesWorkOrder(final GameTestHelper helper)
    {
        helper.assertTrue(StructurePacks.waitUntilFinishedLoading(), "Structure pack discovery was interrupted");
        final ServerLevel level = helper.getLevel();
        BlockPos relativeTownHall = null;
        for (int offset = 65536; offset <= 81920 && relativeTownHall == null; offset += 256)
        {
            final BlockPos candidate = new BlockPos(offset, 1, offset);
            if (IColonyManager.getInstance().isFarEnoughFromColonies(level, helper.absolutePos(candidate)))
            {
                relativeTownHall = candidate;
            }
        }
        helper.assertTrue(relativeTownHall != null,
          "C2S decoration fixture could not find an unclaimed colony position");
        final BlockPos relativeDecoration = relativeTownHall.offset(1, 0, 0);
        final BlockPos townHall = helper.absolutePos(relativeTownHall);
        final BlockPos decoration = helper.absolutePos(relativeDecoration);
        helper.setBlock(relativeTownHall, ModBlocks.blockHutTownHall);
        helper.setBlock(relativeDecoration.below(), Blocks.STONE);

        final ServerPlayer owner = makeNonCreativeServerPlayer(level);
        final IColony colony = IColonyManager.getInstance().createColony(
          level, townHall, owner, "Fabric C2S Decoration Colony", Constants.DEFAULT_STYLE);
        helper.assertTrue(colony != null, "C2S decoration fixture colony was not created");
        // Keep the far-away blueprint footprint resident while the async server callback validates it.
        final int decorationChunkX = decoration.getX() >> 4;
        final int decorationChunkZ = decoration.getZ() >> 4;
        for (int chunkX = decorationChunkX - 6; chunkX <= decorationChunkX + 6; chunkX++)
        {
            for (int chunkZ = decorationChunkZ - 6; chunkZ <= decorationChunkZ + 6; chunkZ++)
            {
                level.setChunkForced(chunkX, chunkZ, true);
                level.getChunk(chunkX, chunkZ);
            }
        }
        ChunkDataHelper.staticClaimInRange(colony.getID(), true, decoration, 4, level, true);
        helper.assertTrue(IColonyManager.getInstance().getColonyByPosFromDim(level.dimension(), decoration) == colony,
          "C2S decoration fixture target was not inside the owner colony");
        helper.assertTrue(colony.getPermissions().hasPermission(owner, Action.MANAGE_HUTS),
          "C2S decoration fixture owner did not receive Manage Huts permission");

        final String blueprintPath = "fundamentals/townhall1.blueprint";
        final NetworkChannel channel = Network.getNetwork();
        final int messageId = findMessageId(channel, DecorationBuildRequestMessage.class);
        helper.assertTrue(messageId > 0, "DecorationBuildRequest message was not registered");
        final DecorationBuildRequestMessage original = new DecorationBuildRequestMessage(
          WorkOrderType.BUILD, decoration, Constants.DEFAULT_STYLE, blueprintPath,
          level.dimension(), Rotation.CLOCKWISE_90, true, BlockPos.ZERO);
        final int communicationId = 0x4445434F;
        final SplitPacketMessage envelope = new SplitPacketMessage(
          communicationId, 0, true, messageId, encode(original));
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.wrappedBuffer(encode(envelope)));
        try
        {
            final MinecraftServer server = level.getServer();
            helper.assertTrue(server != null, "C2S decoration fixture has no running server");
            channel.getRawChannel().handleServerPacket(server, owner, packet);
        }
        finally
        {
            packet.release();
        }

        helper.runAfterDelay(160, () ->
        {
            final WorkOrderDecoration order = colony.getWorkManager().getWorkOrdersOfType(WorkOrderDecoration.class).stream()
              .filter(candidate -> candidate.getLocation().equals(decoration))
              .findFirst()
              .orElse(null);
            helper.assertTrue(order != null,
              "DecorationBuildRequest message did not create the decoration work order");
            helper.assertTrue(order.getWorkOrderType() == WorkOrderType.BUILD,
              "DecorationBuildRequest message created the wrong work-order type: " + order.getWorkOrderType());
            helper.assertTrue(Constants.DEFAULT_STYLE.equals(order.getStructurePack()),
              "DecorationBuildRequest message used the wrong structure pack: " + order.getStructurePack());
            helper.assertTrue(blueprintPath.equals(order.getStructurePath()),
              "DecorationBuildRequest message used the wrong blueprint path: " + order.getStructurePath());
            helper.assertTrue(order.getRotation() == Rotation.CLOCKWISE_90.ordinal(),
              "DecorationBuildRequest message lost the requested rotation: " + order.getRotation());
            helper.assertTrue(order.isMirrored(), "DecorationBuildRequest message lost the mirror flag");
            helper.assertTrue(order.getTargetLevel() == 1,
              "DecorationBuildRequest message created the wrong target level: " + order.getTargetLevel());
            helper.assertTrue(channel.getMessageCache().getIfPresent(communicationId) == null,
              "DecorationBuildRequest envelope remained in the split-packet cache");
            helper.succeed();
        });
    }

    private static Player makeNonCreativeResearchPlayer(final ServerLevel level, final BlockPos position)
    {
        return new Player(level, position, 0.0F,
          new GameProfile(UUID.randomUUID(), "research-test-player"))
        {
            @Override
            public boolean isSpectator()
            {
                return false;
            }

            @Override
            public boolean isCreative()
            {
                return false;
            }

            @Override
            public void displayClientMessage(final net.minecraft.network.chat.Component message, final boolean overlay)
            {
            }

            @Override
            public void playNotifySound(final net.minecraft.sounds.SoundEvent sound, final net.minecraft.sounds.SoundSource source,
              final float volume, final float pitch)
            {
            }
        };
    }

    private static ServerPlayer makeNonCreativeServerPlayer(final ServerLevel level)
    {
        final ServerPlayer player = new ServerPlayer(level.getServer(), level,
          new GameProfile(UUID.randomUUID(), "c2s-research-player"))
        {
            @Override
            public boolean isSpectator()
            {
                return false;
            }

            @Override
            public boolean isCreative()
            {
                return false;
            }
        };
        level.getServer().getPlayerList().placeNewPlayer(new Connection(PacketFlow.SERVERBOUND), player);
        return player;
    }

    private static boolean isMessageRegistered(final NetworkChannel channel, final Class<? extends IMessage> messageClass)
    {
        return findMessageId(channel, messageClass) > 0;
    }

    private static int findMessageId(final NetworkChannel channel, final Class<? extends IMessage> messageClass)
    {
        for (final Map.Entry<Integer, NetworkChannel.NetworkingMessageEntry<?>> entry : channel.getMessagesTypes().entrySet())
        {
            if (messageClass.isInstance(entry.getValue().getCreator().get()))
            {
                return entry.getKey();
            }
        }
        return -1;
    }

    private static byte[] encode(final IMessage message)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try
        {
            message.toBytes(buffer);
            final byte[] bytes = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), bytes);
            return bytes;
        }
        finally
        {
            buffer.release();
        }
    }

    private static void dispatchServerMessage(final NetworkChannel channel, final MinecraftServer server,
      final ServerPlayer player, final int messageId, final int communicationId, final IMessage message)
    {
        final SplitPacketMessage envelope = new SplitPacketMessage(
          communicationId, 0, true, messageId, encode(message));
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.wrappedBuffer(encode(envelope)));
        try
        {
            channel.getRawChannel().handleServerPacket(server, player, packet);
        }
        finally
        {
            packet.release();
        }
    }

    private static final class PriorityProbeEvent extends Event
    {
    }

    private static class InheritedPriorityProbe
    {
        private final StringBuilder invocationOrder;

        private InheritedPriorityProbe(final StringBuilder invocationOrder)
        {
            this.invocationOrder = invocationOrder;
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void inheritedHigh(final PriorityProbeEvent event)
        {
            append("inherited-high");
        }

        protected final void append(final String value)
        {
            if (invocationOrder.length() > 0) invocationOrder.append('|');
            invocationOrder.append(value);
        }
    }

    private static final class PriorityProbe extends InheritedPriorityProbe
    {
        private PriorityProbe(final StringBuilder invocationOrder)
        {
            super(invocationOrder);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void highest(final PriorityProbeEvent event)
        {
            append("highest");
        }

        @SubscribeEvent
        public void normal(final PriorityProbeEvent event)
        {
            append("normal");
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public void low(final PriorityProbeEvent event)
        {
            append("low");
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void lowest(final PriorityProbeEvent event)
        {
            append("lowest");
        }
    }

    private static final class CancellableGameplayEventsProbe
    {
        private boolean pickup;
        private boolean toss;
        private boolean trample;
        private boolean spawn;

        @SubscribeEvent
        public void onPickup(final EntityItemPickupEvent event)
        {
            pickup = true;
            event.setCanceled(true);
        }

        @SubscribeEvent
        public void onToss(final ItemTossEvent event)
        {
            toss = true;
            event.setCanceled(true);
        }

        @SubscribeEvent
        public void onTrample(final BlockEvent.FarmlandTrampleEvent event)
        {
            trample = true;
            event.setCanceled(true);
        }

        @SubscribeEvent
        public void onSpawn(final MobSpawnEvent.PositionCheck event)
        {
            spawn = true;
            event.setResult(Event.Result.DENY);
        }
    }

    private static final class OpeningDataMenu extends AbstractContainerMenu
    {
        private final int openingData;

        private OpeningDataMenu(final int windowId, final int openingData)
        {
            super(MenuType.CRAFTING, windowId);
            this.openingData = openingData;
        }

        @Override
        public boolean stillValid(final Player player)
        {
            return true;
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int slot)
        {
            return ItemStack.EMPTY;
        }
    }
}
