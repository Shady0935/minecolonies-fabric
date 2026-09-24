package com.ldtteam.structurize.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class for localization and sending player messages.
 * Note that MineColonies is still using some of these, so it's not safe to delete yet.
 */
public final class LanguageHandler
{
    /**
     * Private constructor to hide implicit one.
     */
    private LanguageHandler()
    {
        // Intentionally left empty.
    }

    /**
     * Localize a string and use String.format().
     *
     * @param inputKey translation key.
     * @param args     Objects for String.format().
     * @return Localized string.
     */
    public static String format(final String inputKey, final Object... args)
    {
        final String key = inputKey.toLowerCase(Locale.US);
        final String result;
        if (args.length == 0)
        {
            result = Component.translatable(key).getString();
        }
        else
        {
            result = Component.translatable(key, args).getString();
        }
        return result.isEmpty() ? key : result;
    }

    /**
     * Translates key to readable string and formats it.
     *
     * @param key    translation key
     * @param format String.format() attributes
     * @return formatted string
     */
    public static String translateKeyWithFormat(final String key, final Object... format)
    {
        return String.format(translateKey(key), format);
    }

    /**
     * Translates key to readable string.
     *
     * @param key translation key
     * @return readable string
     */
    public static String translateKey(final String key)
    {
        return LanguageCache.getInstance().translateKey(key.toLowerCase(Locale.US));
    }

    /**
     * Sets our cache to use mc default one.
     */
    public static void setMClanguageLoaded()
    {
        LanguageCache.getInstance().isMCloaded = true;
        LanguageCache.getInstance().languageMap = null;
    }

    public static void loadLangPath(final String path)
    {
        LanguageCache.getInstance().load(path);
    }

    private static class LanguageCache
    {
        private static final LanguageCache instance = new LanguageCache();
        private boolean isMCloaded = false;
        private Map<String, String> languageMap;

        private LanguageCache()
        {
            load("assets/structurize/lang/%s.json");
        }

        private void load(final String path)
        {
            for (final String locale : new String[] {"en_us", "default"})
            {
                final String resourcePath = "/" + String.format(path, locale);
                try (InputStream inputStream = LanguageHandler.class.getResourceAsStream(resourcePath))
                {
                    if (inputStream == null)
                    {
                        continue;
                    }

                    try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    {
                        final Map<String, String> loadedLanguageMap = new Gson().fromJson(reader, new TypeToken<Map<String, String>>()
                        {}.getType());
                        languageMap = loadedLanguageMap == null ? Collections.emptyMap() : loadedLanguageMap;
                    }
                    return;
                }
                catch (final IOException exception)
                {
                    throw new IllegalStateException("Could not load language resource " + resourcePath, exception);
                }
            }

            languageMap = Collections.emptyMap();
        }

        private static LanguageCache getInstance()
        {
            return instance;
        }

        private String translateKey(final String key)
        {
            if (isMCloaded)
            {
                return Language.getInstance().getOrDefault(key);
            }
            else
            {
                final String res = languageMap.get(key);
                return res == null ? Language.getInstance().getOrDefault(key) : res;
            }
        }
    }
}
