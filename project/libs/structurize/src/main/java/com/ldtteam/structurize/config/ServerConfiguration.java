package com.ldtteam.structurize.config;

import com.ldtteam.structurize.update.UpdateMode;
import net.minecraft.core.Direction;

import java.util.List;

/** Server configuration with the upstream Forge defaults preserved. */
public final class ServerConfiguration extends AbstractConfiguration
{
    public final ConfigValue<Boolean> ignoreSchematicsFromJar = new ConfigValue<>(false);
    public final ConfigValue<Boolean> allowPlayerSchematics = new ConfigValue<>(true);
    public final ConfigValue<Integer> maxOperationsPerTick = new ConfigValue<>(1000);
    public final ConfigValue<Integer> maxCachedChanges = new ConfigValue<>(10);
    public final ConfigValue<Integer> maxCachedSchematics = new ConfigValue<>(100);
    public final ConfigValue<Integer> maxBlocksChecked = new ConfigValue<>(1000);
    public final ConfigValue<Integer> schematicBlockLimit = new ConfigValue<>(100000);
    public final ConfigValue<String> iteratorType = new ConfigValue<>("default");
    public final ConfigValue<UpdateMode> updateMode = new ConfigValue<>(UpdateMode.DISABLED);
    public final ConfigValue<List<Integer>> updateStartPos = new ConfigValue<>(List.of(-10, -10));
    public final ConfigValue<List<Integer>> updateEndPos = new ConfigValue<>(List.of(10, 10));
    public final ConfigValue<Boolean> teleportAllowed = new ConfigValue<>(true);
    public final ConfigValue<Direction> teleportBuildDirection = new ConfigValue<>(Direction.SOUTH);
    public final ConfigValue<Integer> teleportBuildDistance = new ConfigValue<>(3);
    public final ConfigValue<Boolean> teleportSafety = new ConfigValue<>(true);
}
