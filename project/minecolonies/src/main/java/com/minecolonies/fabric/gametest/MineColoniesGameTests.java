package com.minecolonies.fabric.gametest;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Explosions;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.network.PacketUtils;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.citizen.VisitorCitizen;
import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuilding;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.minecolonies.coremod.network.NetworkChannel;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.coremod.network.messages.client.GlobalQuestSyncMessage;
import com.minecolonies.coremod.network.messages.client.OpenDecoBuildWindowMessage;
import com.minecolonies.coremod.network.messages.client.ServerUUIDMessage;
import com.minecolonies.coremod.network.messages.client.SaveStructureNBTMessage;
import com.minecolonies.coremod.network.messages.splitting.SplitPacketMessage;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.common.extensions.IForgeMenuType;
import com.minecolonies.fabric.event.Event;
import com.minecolonies.fabric.event.ForgeEventFactory;
import com.minecolonies.fabric.event.SubscribeEvent;
import com.minecolonies.fabric.event.entity.item.ItemTossEvent;
import com.minecolonies.fabric.event.entity.living.MobSpawnEvent;
import com.minecolonies.fabric.event.entity.player.ArrowLooseEvent;
import com.minecolonies.fabric.event.entity.player.EntityItemPickupEvent;
import com.minecolonies.fabric.event.level.BlockEvent;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
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

            ServerLivingEntityEvents.MOB_CONVERSION.invoker().onConversion(previous, converted, true);

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
