package com.minecolonies.fabric.gametest;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.network.PacketUtils;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.NetworkChannel;
import com.minecolonies.coremod.network.messages.client.GlobalQuestSyncMessage;
import com.minecolonies.coremod.network.messages.client.OpenDecoBuildWindowMessage;
import com.minecolonies.coremod.network.messages.client.ServerUUIDMessage;
import com.minecolonies.coremod.network.messages.client.SaveStructureNBTMessage;
import com.minecolonies.coremod.network.messages.splitting.SplitPacketMessage;
import com.ldtteam.structurize.storage.StructurePacks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import io.netty.buffer.Unpooled;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

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
    public void networkCodecsAndSplitEnvelopeRoundTrip(final GameTestHelper helper)
    {
        final NetworkChannel channel = Network.getNetwork();
        helper.assertTrue(isMessageRegistered(channel, ServerUUIDMessage.class),
          "Server UUID message was not registered");
        helper.assertTrue(isMessageRegistered(channel, GlobalQuestSyncMessage.class),
          "Global quest message was not registered on the server");
        helper.assertTrue(isMessageRegistered(channel, OpenDecoBuildWindowMessage.class),
          "Build-window message was not registered on the server");
        helper.assertTrue(isMessageRegistered(channel, SaveStructureNBTMessage.class),
          "Scan-save message was not registered on the server");

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
}
