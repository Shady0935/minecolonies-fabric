package com.minecolonies.fabric.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Server-side work cadence controls; loaded before citizen AIs are created. */
public final class CitizenWorkSpeedConfig
{
    private static final Logger LOGGER = LoggerFactory.getLogger("minecolonies/config");
    private static final String SECTION = "[ai_work_speed]";
    private static double workerAiSpeed = 2.0;
    private static double miningSpeed = 2.0;
    private static double placementSpeed = 2.0;
    private static int builderMiningInterval = 1;

    private CitizenWorkSpeedConfig() { }

    public static double workerAiSpeed() { return workerAiSpeed; }
    public static int builderMiningInterval() { return builderMiningInterval; }

    public static int miningDelay(final double ticks)
    {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0.0, ticks / miningSpeed));
    }

    public static int placementDelay(final int ticks)
    {
        return ticks <= 0 ? 0 : Math.max(1, (int) Math.ceil(ticks / placementSpeed));
    }

    /** Preserve other sections and comments, adding only missing work-speed keys. */
    public static void load()
    {
        final Path file = FabricLoader.getInstance().getConfigDir().resolve("minecolonies-server.toml");
        try
        {
            final List<String> lines = Files.exists(file)
              ? new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8)) : new ArrayList<>();
            final List<String> original = new ArrayList<>(lines);
            int start = -1;
            int end = lines.size();
            for (int i = 0; i < lines.size(); i++)
            {
                final String line = lines.get(i).trim().split("#", 2)[0].trim();
                if (line.startsWith("[") && line.endsWith("]"))
                {
                    if (start >= 0)
                    {
                        end = i;
                        break;
                    }
                    if (SECTION.equalsIgnoreCase(line))
                    {
                        start = i;
                    }
                }
            }
            if (start < 0)
            {
                if (!lines.isEmpty()) { lines.add(""); }
                lines.add(SECTION);
                start = lines.size() - 1;
                end = lines.size();
            }

            // Insert missing options before the next section, never under it.
            final List<String> options = new ArrayList<>(lines.subList(start + 1, end));
            workerAiSpeed = readOption(options, "workeraispeedmultiplier", 2.0, 0.25, 8.0, false);
            miningSpeed = readOption(options, "miningspeedmultiplier", 2.0, 0.25, 8.0, false);
            placementSpeed = readOption(options, "builderplacementspeedmultiplier", 2.0, 0.25, 8.0, false);
            builderMiningInterval = (int) readOption(options, "builderminingcheckinterval", 1, 1, 20, true);
            lines.subList(start + 1, end).clear();
            lines.addAll(start + 1, options);
            if (!lines.equals(original))
            {
                Files.createDirectories(file.getParent());
                Files.write(file, lines, StandardCharsets.UTF_8);
            }
            LOGGER.info("Citizen work speed: AI={}x, mining={}x, builder placement={}x, mining checks={} ticks.",
              workerAiSpeed, miningSpeed, placementSpeed, builderMiningInterval);
        }
        catch (final IOException exception)
        {
            LOGGER.error("Could not load citizen work speed from {}; using current defaults.", file, exception);
        }
    }

    private static double readOption(final List<String> lines, final String key, final double defaultValue,
                                     final double min, final double max, final boolean integer)
    {
        final String defaultLine = key + " = " + (integer ? Integer.toString((int) defaultValue) : Double.toString(defaultValue));
        for (int i = 0; i < lines.size(); i++)
        {
            final String line = lines.get(i).trim();
            final int equals = line.indexOf('=');
            if (equals < 0 || !key.equals(line.substring(0, equals).trim())) { continue; }
            try
            {
                final double value = Double.parseDouble(line.substring(equals + 1).split("#", 2)[0].trim());
                if (Double.isFinite(value) && value >= min && value <= max && (!integer || value == Math.rint(value)))
                {
                    return value;
                }
            }
            catch (final NumberFormatException ignored) { }
            LOGGER.warn("Invalid work-speed option {}; using {} (range {} to {}).", key, defaultValue, min, max);
            lines.set(i, defaultLine);
            return defaultValue;
        }
        lines.add(defaultLine);
        return defaultValue;
    }
}
