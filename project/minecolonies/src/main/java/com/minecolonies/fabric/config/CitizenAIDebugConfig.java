package com.minecolonies.fabric.config;

import com.minecolonies.coremod.MineColonies;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Loads and persists the server-side citizen AI debug options. */
public final class CitizenAIDebugConfig
{
    private static final Logger LOGGER = LoggerFactory.getLogger("minecolonies/config");
    private static final String CATEGORY = "[pathfinding]";
    private static final String OVERLAY_OPTION = "citizenaidebugoverlay";
    private static final String LOGGING_OPTION = "citizenaidebuglogging";

    private CitizenAIDebugConfig()
    {
    }

    /** Loads the options from config/minecolonies-server.toml, creating missing entries with their defaults. */
    public static void load()
    {
        final Path configFile = FabricLoader.getInstance().getConfigDir().resolve("minecolonies-server.toml");
        final List<String> lines = new ArrayList<>();
        boolean writeConfig = false;

        try
        {
            if (Files.exists(configFile))
            {
                lines.addAll(Files.readAllLines(configFile, StandardCharsets.UTF_8));
            }
            else
            {
                writeConfig = true;
            }

            int pathfindingHeader = -1;
            int pathfindingEnd = lines.size();
            boolean inPathfinding = false;
            for (int index = 0; index < lines.size(); index++)
            {
                final String trimmed = lines.get(index).trim();
                if (!trimmed.startsWith("[") || !trimmed.endsWith("]"))
                {
                    continue;
                }

                if (inPathfinding)
                {
                    pathfindingEnd = index;
                    inPathfinding = false;
                }
                if (CATEGORY.equalsIgnoreCase(trimmed))
                {
                    pathfindingHeader = index;
                    inPathfinding = true;
                }
            }

            if (pathfindingHeader < 0)
            {
                if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank())
                {
                    lines.add("");
                }
                lines.add(CATEGORY);
                pathfindingHeader = lines.size() - 1;
                pathfindingEnd = lines.size();
                writeConfig = true;
            }

            final boolean[] configChanged = {writeConfig};
            final boolean overlayEnabled = readOrAddOption(lines, pathfindingHeader, pathfindingEnd, OVERLAY_OPTION, false, configFile, configChanged);
            // Keep verbose diagnostics enabled in this temporary investigation build so the first server run is captured.
            final boolean loggingEnabled = readOrAddOption(lines, pathfindingHeader, pathfindingEnd, LOGGING_OPTION, true, configFile, configChanged);
            writeConfig = configChanged[0];

            MineColonies.getConfig().getServer().citizenAiDebugOverlay.set(overlayEnabled);
            MineColonies.getConfig().getServer().citizenAiDebugLogging.set(loggingEnabled);

            if (writeConfig)
            {
                Files.createDirectories(configFile.getParent());
                Files.write(configFile, lines, StandardCharsets.UTF_8);
            }
        }
        catch (final IOException exception)
        {
            LOGGER.error("Could not load or create MineColonies server config at {}.", configFile, exception);
        }
    }

    private static boolean readOrAddOption(final List<String> lines,
                                          final int sectionStart,
                                          final int sectionEnd,
                                          final String option,
                                          final boolean defaultValue,
                                          final Path configFile,
                                          final boolean[] configChanged)
    {
        for (int index = sectionStart + 1; index < sectionEnd; index++)
        {
            final String trimmed = lines.get(index).trim();
            final int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex < 0 || !option.equals(trimmed.substring(0, equalsIndex).trim()))
            {
                continue;
            }

            final String rawValue = trimmed.substring(equalsIndex + 1).split("#", 2)[0].trim();
            if ("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue))
            {
                return Boolean.parseBoolean(rawValue);
            }

            LOGGER.warn("Invalid value for {} in {}; using {}.", option, configFile, defaultValue);
            lines.set(index, option + " = " + defaultValue);
            configChanged[0] = true;
            return defaultValue;
        }

        lines.add(sectionEnd, option + " = " + defaultValue);
        configChanged[0] = true;
        return defaultValue;
    }
}
