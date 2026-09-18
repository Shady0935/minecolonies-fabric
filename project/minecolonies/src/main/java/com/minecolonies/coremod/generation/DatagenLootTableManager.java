package com.minecolonies.coremod.generation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a LootTableManager that's populated on-demand during datagen, so that we
 * can look up other tables for {@link com.minecolonies.coremod.colony.crafting.LootTableAnalyzer}.
 */
public class DatagenLootTableManager extends LootDataManager
{
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private final List<Path>                       resourceRoots;
    private final Map<ResourceLocation, LootTable> tables = new HashMap<>();

    public DatagenLootTableManager()
    {
        super();
        this.resourceRoots = List.of(
                Path.of("src", "main", "resources"),
                Path.of("src", "main", "generated", "resources"));
    }

    @NotNull
    @Override
    public LootTable getLootTable(@NotNull final ResourceLocation location)
    {
        final LootTable table = this.tables.get(location);
        if (table != null) return table;

        final String relativePath = "data/" + location.getNamespace() + "/loot_tables/" + location.getPath() + ".json";
        try (final InputStream inputstream = open(relativePath))
        {
            if (inputstream == null)
            {
                return LootTable.EMPTY;
            }

            try (final Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            )
            {
                final JsonElement jsonobject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                final LootTable loottable = GSON.fromJson(jsonobject, LootTable.class);
                if (loottable != null)
                {
                    this.tables.put(location, loottable);
                    return loottable;
                }
            }
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }

        return LootTable.EMPTY;
    }

    private InputStream open(final String relativePath) throws IOException
    {
        for (final Path root : resourceRoots)
        {
            final Path file = root.resolve(relativePath.replace('/', java.io.File.separatorChar));
            if (Files.isRegularFile(file))
            {
                return Files.newInputStream(file);
            }
        }

        final ClassLoader loader = DatagenLootTableManager.class.getClassLoader();
        return loader.getResourceAsStream(relativePath);
    }
}
