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

/** Loads and persists the server-side citizen AI debug option. */
public final class CitizenAIDebugConfig
{
    private static final Logger LOGGER = LoggerFactory.getLogger("minecolonies/config");
    private static final String CATEGORY = "[pathfinding]";
    private static final String OPTION = "citizenaidebugoverlay";

    private CitizenAIDebugConfig()
    {
    }

    /** Loads the option from config/minecolonies-server.toml, creating it with the default if needed. */
    public static void load()
    {
        final Path configFile = FabricLoader.getInstance().getConfigDir().resolve("minecolonies-server.toml");
        final List<String> lines = new ArrayList<>();
        boolean enabled = false;
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
            int optionLine = -1;
            boolean inPathfinding = false;
            for (int index = 0; index < lines.size(); index++)
            {
                final String trimmed = lines.get(index).trim();
                if (trimmed.startsWith("[") && trimmed.endsWith("]"))
                {
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
                    continue;
                }

                if (inPathfinding && trimmed.startsWith(OPTION))
                {
                    final int equalsIndex = trimmed.indexOf('=');
                    if (equalsIndex >= 0 && OPTION.equals(trimmed.substring(0, equalsIndex).trim()))
                    {
                        optionLine = index;
                        final String rawValue = trimmed.substring(equalsIndex + 1).split("#", 2)[0].trim();
                        if ("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue))
                        {
                            enabled = Boolean.parseBoolean(rawValue);
                        }
                        else
                        {
                            LOGGER.warn("Invalid value for {} in {}; using false.", OPTION, configFile);
                            lines.set(index, OPTION + " = false");
                            writeConfig = true;
                        }
                        break;
                    }
                }
            }

            MineColonies.getConfig().getServer().citizenAiDebugOverlay.set(enabled);

            if (optionLine < 0)
            {
                if (pathfindingHeader >= 0)
                {
                    lines.add(pathfindingEnd, OPTION + " = false");
                }
                else
                {
                    if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank())
                    {
                        lines.add("");
                    }
                    lines.add(CATEGORY);
                    lines.add(OPTION + " = false");
                }
                writeConfig = true;
            }

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
}
