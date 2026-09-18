package com.minecolonies.coremod.generation.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.Log;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.minecolonies.api.quests.QuestParseConstant.*;
import static com.minecolonies.api.quests.registries.QuestRegistries.DIALOGUE_OBJECTIVE_ID;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.coremod.quests.QuestParsingConstants.*;

/**
 * Converts the authored English quest dialogue into translation keys during
 * Fabric datagen while writing the transformed quest JSON files.
 */
public final class QuestTranslationProvider implements DataProvider
{
    private final FabricDataOutput output;

    public QuestTranslationProvider(@NotNull final FabricDataOutput output)
    {
        this.output = output;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "QuestTranslationProvider";
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final PackOutput.PathProvider questProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "quests");
        final List<CompletableFuture<JsonObject>> quests = new ArrayList<>();

        final Path outputFolder = output.getOutputFolder();
        final Path sourceResources = "resources".equals(outputFolder.getFileName().toString())
                                     ? outputFolder.getParent().getParent().resolve("resources")
                                     : outputFolder.getParent().resolve("resources");
        final Path questRoot = sourceResources.resolve("data").resolve(MOD_ID).resolve("quests");

        if (!Files.isDirectory(questRoot))
        {
            throw new IllegalStateException("Authored quests not found at " + questRoot);
        }

        final List<Path> questFiles;
        try (final Stream<Path> walk = Files.walk(questRoot))
        {
            questFiles = walk.filter(Files::isRegularFile)
                             .filter(file -> file.getFileName().toString().endsWith(".json"))
                             .sorted()
                             .toList();
        }
        catch (final IOException exception)
        {
            throw new IllegalStateException("Failed to enumerate quests under " + questRoot, exception);
        }

        for (final Path questFile : questFiles)
        {
            final String relative = questRoot.relativize(questFile).toString().replace('\\', '/');
            final ResourceLocation questPath = new ResourceLocation(MOD_ID, relative.replace(".json", ""));
            final String baseKey = questPath.getNamespace() + ".quests." + questPath.getPath().replace("/", ".");
            final JsonObject langJson = new JsonObject();

            quests.add(CompletableFuture.supplyAsync(() ->
            {
                try
                {
                    final JsonObject questJson;
                    try (final Reader reader = Files.newBufferedReader(questFile))
                    {
                        questJson = GsonHelper.parse(reader);
                    }

                    processQuest(langJson, baseKey, questJson);
                    return questJson;
                }
                catch (final Exception exception)
                {
                    Log.getLogger().error("Failed to process {}", questPath, exception);
                    return null;
                }
            }, Util.backgroundExecutor()).thenComposeAsync(questJson ->
            {
                if (questJson == null)
                {
                    return CompletableFuture.completedFuture(null);
                }

                return DataProvider.saveStable(cache, questJson, questProvider.json(questPath))
                  .thenApply(ignored -> langJson);
            }, Util.backgroundExecutor()));
        }

        return CompletableFuture.allOf(quests.toArray(CompletableFuture[]::new))
          .thenComposeAsync(ignored -> saveLanguage(cache, quests.stream().map(CompletableFuture::join).toList()),
            Util.backgroundExecutor());
    }

    @NotNull
    private CompletableFuture<?> saveLanguage(@NotNull final CachedOutput cache,
                                              @NotNull final List<JsonObject> languageObjects)
    {
        final JsonObject language = new JsonObject();
        for (final JsonObject languageObject : languageObjects)
        {
            if (languageObject == null)
            {
                continue;
            }

            for (final Map.Entry<String, JsonElement> entry : languageObject.entrySet())
            {
                language.add(entry.getKey(), entry.getValue());
            }
        }

        final PackOutput.PathProvider languageProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang");
        final Path languageFile = languageProvider.file(new ResourceLocation(MOD_ID, "quests"), "json");
        return DataProvider.saveStable(cache, language, languageFile);
    }

    private static void processQuest(final JsonObject language, final String baseKey, final JsonObject json)
    {
        final String name = json.get(NAME).getAsString();
        language.addProperty(baseKey, name);
        json.addProperty(NAME, baseKey);

        int objectiveCount = 0;
        for (final JsonElement objectiveJson : json.get(QUEST_OBJECTIVES).getAsJsonArray())
        {
            final String objectiveKey = baseKey + ".obj" + objectiveCount;
            processObjective(language, objectiveKey, objectiveJson.getAsJsonObject());
            ++objectiveCount;
        }
    }

    private static void processObjective(final JsonObject language, final String baseKey, final JsonObject json)
    {
        final ResourceLocation type = new ResourceLocation(json.get(TYPE).getAsString());
        if (!type.equals(DIALOGUE_OBJECTIVE_ID))
        {
            return;
        }

        language.addProperty(baseKey, json.get(TEXT_ID).getAsString());
        json.addProperty(TEXT_ID, baseKey);

        int answerCount = 0;
        for (final JsonElement answerJson : json.get(OPTIONS_ID).getAsJsonArray())
        {
            final String answerKey = baseKey + ".answer" + answerCount;
            final JsonObject answer = answerJson.getAsJsonObject();
            language.addProperty(answerKey, answer.get(ANSWER_ID).getAsString());
            answer.addProperty(ANSWER_ID, answerKey);
            processObjective(language, answerKey + ".reply", answer.get(RESULT_ID).getAsJsonObject());
            ++answerCount;
        }
    }
}
