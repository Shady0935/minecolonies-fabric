package com.minecolonies.fabric.config;
import com.ldtteam.structurize.config.fabric.FabricConfigSpec;
import com.ldtteam.structurize.config.fabric.FabricConfigFile;
import com.ldtteam.structurize.config.fabric.ConfigSnapshotCodec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class FabricConfigPersistenceTest
{
    @TempDir Path directory;
    enum Policy { KEEP, REMOVE }
    private static class Fixture
    {
        final FabricConfigSpec.Builder builder = new FabricConfigSpec.Builder();
        final FabricConfigSpec.BooleanValue enabled;
        final FabricConfigSpec.IntValue count;
        final FabricConfigSpec.LongValue large;
        final FabricConfigSpec.DoubleValue speed;
        final FabricConfigSpec.ConfigValue<List<? extends String>> list;
        final FabricConfigSpec.EnumValue<Policy> policy;
        final FabricConfigSpec spec;
        Fixture()
        {
            builder.push("gameplay");
            enabled = builder.comment("Enable gameplay").define("enabled", true);
            count = builder.defineInRange("count", 4, 1, 20);
            large = builder.defineInRange("large", 9007199254740993L, 1L, Long.MAX_VALUE);
            speed = builder.defineInRange("speed", 1.0, 0.25, 8.0);
            list = builder.defineList("materials", List.of("minecraft:stone"), x -> x instanceof String);
            policy = builder.defineEnum("policy", Policy.KEEP);
            builder.pop();
            spec = builder.build();
        }
    }

    @Test void createsAllDefaultsAndComments() throws Exception
    {
        final Fixture fixture = new Fixture();
        final Path path = directory.resolve("new/server.toml");
        FabricConfigFile.load(fixture.spec, path);
        final String text = Files.readString(path);
        assertTrue(text.contains("[gameplay]"));
        assertTrue(text.contains("Enable gameplay"));
        assertTrue(text.contains("Range: 1 to 20"));
        assertEquals(6, fixture.spec.snapshot().size());
        assertEquals(9007199254740993L, fixture.large.get());
    }

    @Test void migratesExistingAiOnlyFileWithoutLosingSettings() throws Exception
    {
        final Path path = directory.resolve("server.toml");
        final String original = "# User tuning\n[pathfinding]\ncitizenaidebugoverlay = true\n[ai_work_speed]\nworkeraispeedmultiplier = 2.0\n";
        Files.writeString(path, original);
        FabricConfigFile.load(new Fixture().spec, path);
        final String updated = Files.readString(path);
        assertTrue(updated.contains("citizenaidebugoverlay = true"));
        assertTrue(updated.contains("workeraispeedmultiplier = 2.0"));
        assertTrue(updated.contains("User tuning"));
        try (var files = Files.list(directory))
        {
            final Path backup = files.filter(file -> file.toString().endsWith(".bak")).findFirst().orElseThrow();
            assertEquals(original, Files.readString(backup));
        }
    }

    @Test void readsMultilineListsEscapesEnumsAndNumbers() throws Exception
    {
        final Path path = directory.resolve("server.toml");
        Files.writeString(path, "[gameplay]\nenabled=false\ncount=12\nlarge=9007199254740995\nspeed=2.5\npolicy='REMOVE'\nmaterials=[\n 'minecraft:oak_log',\n \"quoted\\\"block\", # comment\n]\n");
        final Fixture fixture = new Fixture();
        FabricConfigFile.load(fixture.spec, path);
        assertFalse(fixture.enabled.get());
        assertEquals(12, fixture.count.get());
        assertEquals(9007199254740995L, fixture.large.get());
        assertEquals(2.5, fixture.speed.get());
        assertEquals(Policy.REMOVE, fixture.policy.get());
        assertEquals(List.of("minecraft:oak_log", "quoted\"block"), fixture.list.get());
    }

    @Test void invalidTypesRangesAndNonFiniteValuesRestoreDefaults() throws Exception
    {
        final Path path = directory.resolve("server.toml");
        Files.writeString(path, "[gameplay]\nenabled='true'\ncount=1000\nlarge=1.5\nspeed=nan\npolicy='INVALID'\nmaterials=[false]\n");
        final Fixture fixture = new Fixture();
        FabricConfigFile.load(fixture.spec, path);
        assertTrue(fixture.enabled.get());
        assertEquals(4, fixture.count.get());
        assertEquals(9007199254740993L, fixture.large.get());
        assertEquals(1.0, fixture.speed.get());
        assertEquals(Policy.KEEP, fixture.policy.get());
        assertEquals(List.of("minecraft:stone"), fixture.list.get());
        assertThrows(IllegalArgumentException.class, () -> fixture.speed.set(Double.NaN));
    }

    @Test void malformedTomlPreservesFileAndLiveValues() throws Exception
    {
        final Path path = directory.resolve("bad.toml");
        final String invalid = "[gameplay\ncount = 19";
        Files.writeString(path, invalid);
        final Fixture fixture = new Fixture();
        fixture.count.set(7);
        assertThrows(IllegalStateException.class, () -> FabricConfigFile.load(fixture.spec, path));
        assertEquals(invalid, Files.readString(path));
        assertEquals(7, fixture.count.get());
    }

    @Test void stableSecondLoadDoesNotRewriteOrCreateAnotherBackup() throws Exception
    {
        final Fixture fixture = new Fixture();
        final Path path = directory.resolve("server.toml");
        FabricConfigFile.load(fixture.spec, path);
        final String first = Files.readString(path);
        FabricConfigFile.load(fixture.spec, path);
        assertEquals(first, Files.readString(path));
        try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
    }

    @Test void remoteSnapshotAndDisconnectRestorationDoNotChangeDisk() throws Exception
    {
        final Fixture client = new Fixture(), server = new Fixture(), common = new Fixture();
        final Path path = directory.resolve("client-local-server.toml");
        FabricConfigFile.load(client.spec, path);
        final Map<String, Object> local = client.spec.snapshot();
        final String before = Files.readString(path);
        server.count.set(19);
        server.policy.set(Policy.REMOVE);
        ConfigSnapshotCodec.apply(ConfigSnapshotCodec.encode(server.spec, common.spec), client.spec, new Fixture().spec);
        assertEquals(19, client.count.get());
        assertEquals(Policy.REMOVE, client.policy.get());
        assertEquals(before, Files.readString(path));
        client.spec.applySnapshot(local);
        assertEquals(4, client.count.get());
        assertEquals(9007199254740993L, client.large.get());
    }

    @Test void invalidSnapshotIsAtomicAcrossServerAndCommon()
    {
        final Fixture client = new Fixture(), common = new Fixture(), server = new Fixture();
        server.count.set(19);
        final String invalid = ConfigSnapshotCodec.encode(server.spec, server.spec).replace("\"policy\":\"KEEP\"", "\"policy\":\"INVALID\"");
        // Keys are qualified in snapshots; explicitly corrupt the common policy only.
        final var json = com.google.gson.JsonParser.parseString(invalid).getAsJsonObject();
        json.getAsJsonObject("common").addProperty("gameplay.policy", "INVALID");
        assertThrows(IllegalArgumentException.class, () -> ConfigSnapshotCodec.apply(json.toString(), client.spec, common.spec));
        assertEquals(4, client.count.get());
        assertEquals(4, common.count.get());
    }

    @Test void nestedCategoriesAndCaseMatchOriginalKeys()
    {
        final var builder = new FabricConfigSpec.Builder();
        builder.push("requestSystem").push("nested").define("maxColonySize", 20);
        assertTrue(builder.build().snapshot().containsKey("requestSystem.nested.maxColonySize"));
    }

    @Test void snapshotsDoNotAliasMutableLists()
    {
        final Fixture fixture = new Fixture();
        final ArrayList<String> mutable = new ArrayList<>(List.of("minecraft:dirt"));
        fixture.list.set(mutable);
        final var snapshot = fixture.spec.snapshot();
        mutable.add("minecraft:stone");
        assertEquals(List.of("minecraft:dirt"), snapshot.get("gameplay.materials"));
    }
}
