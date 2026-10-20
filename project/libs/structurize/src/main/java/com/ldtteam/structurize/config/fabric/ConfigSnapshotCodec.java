package com.ldtteam.structurize.config.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Versioned data-only snapshots; never deserialize Java classes or persist received values. */
public final class ConfigSnapshotCodec
{
    public static final int MAX_LENGTH = 131072;
    private ConfigSnapshotCodec() { }

    public static String encode(final FabricConfigSpec server, final FabricConfigSpec common)
    {
        return new Gson().toJson(Map.of("version", 1, "server", server.snapshot(), "common", common.snapshot()));
    }

    public static void apply(final String json, final FabricConfigSpec server, final FabricConfigSpec common)
    {
        if (json.length() > MAX_LENGTH) { throw new IllegalArgumentException("Configuration snapshot too large"); }
        final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (root.get("version").getAsInt() != 1) { throw new IllegalArgumentException("Unknown configuration snapshot version"); }
        final Map<String, Object> serverValues = decode(root.getAsJsonObject("server"));
        final Map<String, Object> commonValues = decode(root.getAsJsonObject("common"));
        server.validateSnapshot(serverValues);
        common.validateSnapshot(commonValues);
        server.applySnapshot(serverValues);
        common.applySnapshot(commonValues);
    }

    private static Map<String, Object> decode(final JsonObject object)
    {
        final Map<String, Object> values = new LinkedHashMap<>();
        object.entrySet().forEach(entry -> values.put(entry.getKey(), value(entry.getValue())));
        return values;
    }

    private static Object value(final JsonElement element)
    {
        if (element.isJsonArray())
        {
            final ArrayList<Object> list = new ArrayList<>();
            element.getAsJsonArray().forEach(item -> list.add(value(item)));
            return list;
        }
        if (!element.isJsonPrimitive()) { throw new IllegalArgumentException("Only scalar config values and lists are supported"); }
        final var primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) { return primitive.getAsBoolean(); }
        if (primitive.isNumber()) { return primitive.getAsBigDecimal(); }
        return primitive.getAsString();
    }
}
