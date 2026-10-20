package com.minecolonies.fabric.gametest;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.config.fabric.ConfigSnapshotCodec;
import com.ldtteam.structurize.config.fabric.FabricConfigFile;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.fabric.common.brewing.BrewingRecipeRegistry;
import com.minecolonies.fabric.config.FabricConfiguration;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

/** Real-loader probes for full schemas, migration, server authority and the brewing bridge. */
public final class MineColoniesConfigurationGameTests implements FabricGameTest
{
    private static void passed(final net.minecraft.gametest.framework.GameTestHelper helper)
    {
        helper.succeed();
        org.slf4j.LoggerFactory.getLogger("minecolonies/config-audit").info("Configuration parity probe passed at {}", helper.absolutePos(net.minecraft.core.BlockPos.ZERO));
    }
    private static final class CancelProbe extends com.minecolonies.fabric.event.Event { }
    private static final class CancellationListeners
    {
        int ordinary, canceled;
        @com.minecolonies.fabric.event.SubscribeEvent(priority = com.minecolonies.fabric.event.EventPriority.HIGHEST)
        public void cancel(final CancelProbe event) { event.setCanceled(true); }
        @com.minecolonies.fabric.event.SubscribeEvent
        public void ordinary(final CancelProbe event) { ordinary++; }
        @com.minecolonies.fabric.event.SubscribeEvent(receiveCanceled = true)
        public void canceled(final CancelProbe event) { canceled++; }
    }

    @GameTestGenerator
    public Collection<TestFunction> configurationTests()
    {
        return List.of(
            new TestFunction("minecolonies_config", "inventory_provider_lifetime", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                final var inventory = new net.minecraft.world.SimpleContainer(3);
                final var first = com.minecolonies.fabric.capability.CapabilityHooks.getCapability(inventory, com.minecolonies.fabric.capability.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.UP).orElseThrow();
                final var second = com.minecolonies.fabric.capability.CapabilityHooks.getCapability(inventory, com.minecolonies.fabric.capability.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.DOWN).orElseThrow();
                helper.assertTrue(first == second, "An unsided container must share its live handler across all faces");
                final var furnace = new net.minecraft.world.level.block.entity.FurnaceBlockEntity(net.minecraft.core.BlockPos.ZERO, net.minecraft.world.level.block.Blocks.FURNACE.defaultBlockState());
                final var top = com.minecolonies.fabric.capability.CapabilityHooks.getCapability(furnace, com.minecolonies.fabric.capability.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.UP).orElseThrow();
                helper.assertTrue(top == com.minecolonies.fabric.capability.CapabilityHooks.getCapability(furnace, com.minecolonies.fabric.capability.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.UP).orElseThrow(), "Live sided handlers must be reused");
                furnace.setRemoved();
                helper.assertTrue(!com.minecolonies.fabric.capability.CapabilityHooks.getCapability(furnace, com.minecolonies.fabric.capability.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.UP).isPresent(), "Removed block entities must not expose a new inventory handle");
                passed(helper);
            }),
            new TestFunction("minecolonies_config", "synchronous_chunk_visibility", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                final var position = new net.minecraft.core.BlockPos(560003, -59, 560003);
                helper.getLevel().getChunk(position);
                helper.assertTrue(com.minecolonies.api.util.WorldUtil.isBlockLoaded(helper.getLevel(), position), "A synchronously loaded chunk must be visible to block access in the same tick");
                passed(helper);
            }),
            new TestFunction("minecolonies_config", "event_cancellation_contract", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                final var bus = new com.minecolonies.fabric.common.MinecraftForge.EventBus();
                final var listeners = new CancellationListeners();
                bus.register(listeners);
                helper.assertTrue(bus.post(new CancelProbe()), "Cancellation must propagate to dispatcher");
                helper.assertTrue(listeners.ordinary == 0 && listeners.canceled == 1, "Only opt-in listeners may receive canceled events");
                passed(helper);
            }),
            new TestFunction("minecolonies_config", "mapped_vanilla_access", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                final var stand = new net.minecraft.world.level.block.entity.BrewingStandBlockEntity(net.minecraft.core.BlockPos.ZERO, net.minecraft.world.level.block.Blocks.BREWING_STAND.defaultBlockState());
                final var tag = new net.minecraft.nbt.CompoundTag();
                tag.putShort("BrewTime", (short)121);
                stand.load(tag);
                helper.assertTrue(com.minecolonies.fabric.compat.FabricVanillaCompat.getBrewingTime(stand) == 121, "Brewing timer accessor must work in remapped JARs");
                final var explosion = new net.minecraft.world.level.Explosion(helper.getLevel(), null, 1.25, 2.5, 3.75, 0, false, net.minecraft.world.level.Explosion.BlockInteraction.KEEP);
                helper.assertTrue(com.minecolonies.fabric.compat.FabricVanillaCompat.getExplosionPosition(explosion).equals(new net.minecraft.world.phys.Vec3(1.25, 2.5, 3.75)), "Explosion coordinates must remain exact");
                final var spawner = new net.minecraft.world.level.block.entity.SpawnerBlockEntity(net.minecraft.core.BlockPos.ZERO, net.minecraft.world.level.block.Blocks.SPAWNER.defaultBlockState()).getSpawner();
                com.minecolonies.fabric.compat.FabricVanillaCompat.setSpawnerRequiredPlayerRange(spawner, 17);
                helper.assertTrue(spawner.save(new net.minecraft.nbt.CompoundTag()).getShort("RequiredPlayerRange") == 17, "Spawner range accessor must persist");
                helper.assertTrue(com.minecolonies.fabric.compat.FabricVanillaCompat.navigatingMobs(helper.getLevel()) != null, "Navigation collection must be accessible");
                passed(helper);
            }),
            new TestFunction("minecolonies_config", "inventory_face_contract", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                final var furnace = new net.minecraft.world.level.block.entity.FurnaceBlockEntity(net.minecraft.core.BlockPos.ZERO, net.minecraft.world.level.block.Blocks.FURNACE.defaultBlockState());
                furnace.setLevel(helper.getLevel());
                final var top = com.minecolonies.fabric.capability.CapabilityHooks.getCapability(furnace, com.minecolonies.fabric.capability.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.UP).orElseThrow();
                final var bottom = com.minecolonies.fabric.capability.CapabilityHooks.getCapability(furnace, com.minecolonies.fabric.capability.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.DOWN).orElseThrow();
                helper.assertTrue(top.getSlots() == 1 && bottom.getSlots() == 2, "Furnace face slots must match vanilla");
                helper.assertTrue(top.insertItem(0, new ItemStack(Items.IRON_ORE), true).isEmpty() && furnace.isEmpty(), "Simulation must not mutate inventory");
                top.insertItem(0, new ItemStack(Items.IRON_ORE), false);
                helper.assertTrue(furnace.getItem(0).is(Items.IRON_ORE), "Face slot must map to actual input");
                helper.assertTrue(bottom.insertItem(0, new ItemStack(Items.IRON_ORE), false).is(Items.IRON_ORE), "Output slot must reject insertion");
                furnace.setItem(1, new ItemStack(Items.COAL));
                helper.assertTrue(bottom.extractItem(1, 1, false).isEmpty(), "Bottom must not extract fuel");
                furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 3));
                helper.assertTrue(bottom.extractItem(0, 2, true).getCount() == 2 && furnace.getItem(2).getCount() == 3, "Extraction simulation must preserve output");
                helper.assertTrue(bottom.extractItem(0, 2, false).getCount() == 2 && furnace.getItem(2).getCount() == 1, "Extraction must remove exactly the requested amount");
                final var stand = new net.minecraft.world.level.block.entity.BrewingStandBlockEntity(net.minecraft.core.BlockPos.ZERO, net.minecraft.world.level.block.Blocks.BREWING_STAND.defaultBlockState());
                final var bottles = new com.minecolonies.fabric.inventory.InvWrapper(stand);
                helper.assertTrue(bottles.insertItem(0, new ItemStack(Items.GLASS_BOTTLE, 8), false).getCount() == 7 && stand.getItem(0).getCount() == 1, "Bottle slot must be limited to one item");
                passed(helper);
            }),
            new TestFunction("minecolonies_config", "config_complete_schema", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                helper.assertTrue(MineColonies.getConfig().serverSpec().options().size() == 93, "Server schema must expose all 93 options");
                helper.assertTrue(MineColonies.getConfig().clientSpec().options().size() == 5, "Client schema must retain five options");
                helper.assertTrue(MineColonies.getConfig().commonSpec().options().size() == 2, "Common schema must retain two options");
                helper.assertTrue(Structurize.getConfig().serverSpec().options().size() == 15, "Structurize must expose all fifteen options");
                helper.assertTrue(Files.isRegularFile(FabricConfiguration.activeServerFile()), "Effective server config must exist");
                helper.assertTrue(Files.isRegularFile(FabricLoader.getInstance().getConfigDir().resolve("structurize-server.toml")), "Structurize config must exist");
                passed(helper);
            }),
            new TestFunction("minecolonies_config", "config_real_schema_migration", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                try
                {
                    final var directory = FabricLoader.getInstance().getGameDir().resolve("config-audit-fixture");
                    Files.createDirectories(directory);
                    final var file = directory.resolve("migrated-server.toml");
                    Files.writeString(file, "[pathfinding]\ncitizenaidebugoverlay=true\n[ai_work_speed]\nworkeraispeedmultiplier=2.0\n[gameplay]\nmaxcitizenpercolony=350\n");
                    final Configuration configuration = new Configuration();
                    FabricConfigFile.load(configuration.serverSpec(), file);
                    helper.assertTrue(configuration.getServer().maxCitizenPerColony.get() == 350, "Actual gameplay value must load from TOML");
                    helper.assertTrue(configuration.getServer().citizenAiDebugOverlay.get(), "AI overlay setting must survive migration");
                    helper.assertTrue(Files.readString(file).contains("workeraispeedmultiplier"), "Unknown work-speed section must survive migration");
                    helper.assertTrue(configuration.serverSpec().snapshot().size() == 93, "All original server settings must remain available");
                    passed(helper);
                }
                catch (final java.io.IOException exception) { throw new IllegalStateException(exception); }
            }),
            new TestFunction("minecolonies_config", "config_remote_authority", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                final Configuration server = new Configuration(), client = new Configuration();
                final var local = client.serverSpec().snapshot();
                server.getServer().maxColonySize.set(37);
                server.getCommon().rsEnableDebugLogging.set(true);
                ConfigSnapshotCodec.apply(ConfigSnapshotCodec.encode(server.serverSpec(), server.commonSpec()), client.serverSpec(), client.commonSpec());
                helper.assertTrue(client.getServer().maxColonySize.get() == 37, "Server setting must override remote client memory");
                helper.assertTrue(client.getCommon().rsEnableDebugLogging.get(), "Common settings must synchronize too");
                helper.assertTrue(client.getClient().citizenVoices.get(), "Client-only preferences must remain local");
                client.serverSpec().applySnapshot(local);
                helper.assertTrue(client.getServer().maxColonySize.get() == 20, "Local setting must restore after remote snapshot");
                helper.assertTrue(!ServerPlayNetworking.getGlobalReceivers().contains(FabricConfiguration.CHANNEL), "Clients must not be able to send configuration overrides to the server");
                passed(helper);
            }),
            new TestFunction("minecolonies_config", "brewing_vanilla_bridge", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                final ItemStack water = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                final ItemStack awkward = BrewingRecipeRegistry.getOutput(new ItemStack(Items.NETHER_WART), water);
                helper.assertTrue(!awkward.isEmpty() && PotionUtils.getPotion(awkward) == Potions.AWKWARD, "Water and nether wart must produce awkward potion");
                helper.assertTrue(PotionUtils.getPotion(water) == Potions.WATER, "Brewing lookup must not mutate input");
                final ItemStack splash = BrewingRecipeRegistry.getOutput(new ItemStack(Items.GUNPOWDER), water);
                helper.assertTrue(splash.is(Items.SPLASH_POTION), "Gunpowder must produce splash potion");
                helper.assertTrue(BrewingRecipeRegistry.getOutput(new ItemStack(Items.DRAGON_BREATH), splash).is(Items.LINGERING_POTION), "Dragon breath must produce lingering potion");
                helper.assertTrue(BrewingRecipeRegistry.getOutput(new ItemStack(Items.STONE), water).isEmpty(), "Invalid brewing input must have no output");
                helper.assertTrue(!BrewingRecipeRegistry.getRecipes().isEmpty(), "Recipe discovery must include the vanilla brewing adapter");
                passed(helper);
            }));
    }
}
