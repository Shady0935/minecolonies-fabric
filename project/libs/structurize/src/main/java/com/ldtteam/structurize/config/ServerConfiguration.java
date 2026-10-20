package com.ldtteam.structurize.config;

import com.ldtteam.structurize.config.fabric.FabricConfigSpec;
import com.ldtteam.structurize.update.UpdateMode;
import net.minecraft.core.Direction;
import java.util.List;

/** Original Structurize 1.20.1 keys, defaults and numeric bounds. */
public final class ServerConfiguration extends AbstractConfiguration
{
    public final ConfigValue<Boolean> ignoreSchematicsFromJar;
    public final ConfigValue<Boolean> allowPlayerSchematics;
    public final ConfigValue<Integer> maxOperationsPerTick;
    public final ConfigValue<Integer> maxCachedChanges;
    public final ConfigValue<Integer> maxCachedSchematics;
    public final ConfigValue<Integer> maxBlocksChecked;
    public final ConfigValue<Integer> schematicBlockLimit;
    public final ConfigValue<String> iteratorType;
    public final ConfigValue<UpdateMode> updateMode;
    public final ConfigValue<List<Integer>> updateStartPos;
    public final ConfigValue<List<Integer>> updateEndPos;
    public final ConfigValue<Boolean> teleportAllowed;
    public final ConfigValue<Direction> teleportBuildDirection;
    public final ConfigValue<Integer> teleportBuildDistance;
    public final ConfigValue<Boolean> teleportSafety;

    public ServerConfiguration() { this(new FabricConfigSpec.Builder()); }

    ServerConfiguration(final FabricConfigSpec.Builder builder)
    {
        builder.push("gameplay");
        ignoreSchematicsFromJar = builder.comment("Ignore default schematics from mod JARs (restart required).")
            .defineValue("ignoreSchematicsFromJar", new ConfigValue<>(false), null, null, null);
        allowPlayerSchematics = builder.comment("Allow player-created schematics.")
            .defineValue("allowPlayerSchematics", new ConfigValue<>(true), null, null, null);
        maxOperationsPerTick = integer(builder, "maxOperationsPerTick", 1000, 0, 100000);
        maxCachedChanges = integer(builder, "maxCachedChanges", 10, 0, 100);
        maxCachedSchematics = integer(builder, "maxCachedSchematics", 100, 0, 100000);
        maxBlocksChecked = integer(builder, "maxBlocksChecked", 1000, 0, 100000);
        schematicBlockLimit = integer(builder, "schematicBlockLimit", 100000, 1000, 1000000);
        iteratorType = builder.defineValue("iteratorType", new ConfigValue<>("default"), null, null, null);
        builder.pop().push("teleport");
        teleportAllowed = builder.defineValue("teleportAllowed", new ConfigValue<>(true), null, null, null);
        teleportBuildDirection = builder.defineValue("teleportBuildDirection", new ConfigValue<>(Direction.SOUTH), null, null, null);
        teleportBuildDistance = integer(builder, "teleportBuildDistance", 3, 1, 16);
        teleportSafety = builder.defineValue("teleportSafety", new ConfigValue<>(true), null, null, null);
        builder.pop().push("update");
        updateMode = builder.defineValue("mode", new ConfigValue<>(UpdateMode.DISABLED), null, null, null);
        updateStartPos = position(builder, "start", List.of(-10, -10));
        updateEndPos = position(builder, "end", List.of(10, 10));
        builder.pop();
    }

    private static ConfigValue<Integer> integer(final FabricConfigSpec.Builder builder, final String key,
                                                 final int value, final int min, final int max)
    { return builder.defineValue(key, new ConfigValue<>(value), min, max, null); }

    private static ConfigValue<List<Integer>> position(final FabricConfigSpec.Builder builder, final String key, final List<Integer> value)
    {
        return builder.defineValue(key, new ConfigValue<>(value) {
            @Override public boolean accepts(final Object raw) { return raw instanceof List<?> list && list.size() == 2; }
        }, null, null, item -> item instanceof Integer);
    }
}
