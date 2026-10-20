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
    @GameTestGenerator
    public Collection<TestFunction> configurationTests()
    {
        return List.of(
            new TestFunction("minecolonies_config", "config_complete_schema", FabricGameTest.EMPTY_STRUCTURE, 40, 0, true, helper -> {
                helper.assertTrue(MineColonies.getConfig().serverSpec().options().size() == 88, "Server schema must expose all 88 options");
                helper.assertTrue(MineColonies.getConfig().clientSpec().options().size() == 5, "Client schema must retain five options");
                helper.assertTrue(MineColonies.getConfig().commonSpec().options().size() == 2, "Common schema must retain two options");
                helper.assertTrue(Structurize.getConfig().serverSpec().options().size() == 15, "Structurize must expose all fifteen options");
                helper.assertTrue(Files.isRegularFile(FabricConfiguration.activeServerFile()), "Effective server config must exist");
                helper.assertTrue(Files.isRegularFile(FabricLoader.getInstance().getConfigDir().resolve("structurize-server.toml")), "Structurize config must exist");
                helper.succeed();
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
                    helper.assertTrue(configuration.serverSpec().snapshot().size() == 88, "All original server settings must remain available");
                    helper.succeed();
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
                helper.succeed();
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
                helper.succeed();
            }));
    }
}
