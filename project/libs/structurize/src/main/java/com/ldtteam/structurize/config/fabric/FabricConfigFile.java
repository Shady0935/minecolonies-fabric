package com.ldtteam.structurize.config.fabric;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Full TOML persistence, independent of Minecraft so migration and validation can be verified in isolation. */
public final class FabricConfigFile
{
    private FabricConfigFile() { }

    public static void load(final FabricConfigSpec spec, final Path file)
    {
        try
        {
            final boolean existed = Files.exists(file);
            final String original = existed ? Files.readString(file, StandardCharsets.UTF_8) : "";
            // Parse before touching live values. Syntax errors never overwrite an administrator's file.
            final CommentedConfig config = existed ? new TomlParser().parse(original) : TomlFormat.newConfig();
            final Map<String, Object> resolved = new LinkedHashMap<>();
            boolean changed = !existed;
            for (final Map.Entry<String, FabricConfigSpec.Option> item : spec.options().entrySet())
            {
                final FabricConfigSpec.Option option = item.getValue();
                final Object raw = config.get(option.path());
                Object value;
                try { value = option.normalize(raw); }
                catch (final IllegalArgumentException exception)
                {
                    value = option.defaultValue();
                    if (raw != null) { LoggerFactory.getLogger("minecolonies/config").warn("Invalid option {} in {}; restoring default.", item.getKey(), file); }
                }
                final Object stored = FabricConfigSpec.serializable(value);
                resolved.put(item.getKey(), stored);
                if (!Objects.equals(raw, stored)) { config.set(option.path(), stored); changed = true; }
                if (config.getComment(option.path()) == null) { config.setComment(option.path(), option.comment()); changed = true; }
            }
            // Validate the entire result before writing or applying any values.
            spec.validateSnapshot(resolved);
            if (changed)
            {
                final String output = new TomlWriter().writeToString(config);
                final Path absolute = file.toAbsolutePath();
                Files.createDirectories(absolute.getParent());
                if (existed)
                {
                    Files.copy(absolute, absolute.resolveSibling(absolute.getFileName() + ".before-fabric-"
                        + UUID.randomUUID().toString().substring(0, 8) + ".bak"));
                }
                final Path temporary = Files.createTempFile(absolute.getParent(), absolute.getFileName().toString(), ".tmp");
                try
                {
                    Files.writeString(temporary, output, StandardCharsets.UTF_8);
                    try { Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                    catch (final AtomicMoveNotSupportedException ignored)
                    { Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING); }
                }
                finally { Files.deleteIfExists(temporary); }
            }
            spec.applySnapshot(resolved);
            LoggerFactory.getLogger("minecolonies/config").info("Loaded {} options from {}", spec.options().size(), file);
        }
        catch (final IOException | RuntimeException exception)
        {
            throw new IllegalStateException("Cannot load configuration " + file + "; original file preserved. Fix the TOML and restart.", exception);
        }
    }
}
