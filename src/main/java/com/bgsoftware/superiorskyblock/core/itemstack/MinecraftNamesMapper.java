package com.bgsoftware.superiorskyblock.core.itemstack;

import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftNamesMapper {

    private static final Pattern MINECRAFT_KEY_PATTERN = Pattern.compile("minecraft:(.+)");
    private static final String MINECRAFT_NAMES_MAPPING_URL = "https://bg-software.com/minecraft_mappings.json";
    private static final String MINECRAFT_NAMES_MAPPING_LEGACY_URL = "https://bg-software.com/minecraft_mappings_legacy.json";
    private static final Gson GSON = new Gson();

    public static Optional<String> getMinecraftName(String name) {
        Matcher matcher = MINECRAFT_KEY_PATTERN.matcher(name);
        return matcher.matches() ? Optional.of(matcher.group(1).toLowerCase(Locale.ENGLISH)) : Optional.empty();
    }

    private final Map<Class<?>, Map<String, String>> enumNamesMapping;

    public MinecraftNamesMapper() {
        this.enumNamesMapping = fetchEnumNamesMapping();
    }

    public Optional<String> getMappedName(Class<?> expectedEnumClass, String name) {
        return Optional.ofNullable(enumNamesMapping.get(expectedEnumClass))
                .map(mappings -> mappings.get(name));
    }

    private static Map<Class<?>, Map<String, String>> fetchEnumNamesMapping() {
        Map<Class<?>, Map<String, String>> mappedNames = new HashMap<>();

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(ServerVersion.isLegacy() ?
                    MINECRAFT_NAMES_MAPPING_LEGACY_URL : MINECRAFT_NAMES_MAPPING_URL).openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
            connection.setDoInput(true);

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                JsonElement mappingsElement = GSON.fromJson(reader, JsonElement.class);
                if (!mappingsElement.isJsonArray())
                    throw new MappingFormatException("Expected to receive json array, received " + mappingsElement.getClass());

                JsonArray mappings = mappingsElement.getAsJsonArray();

                for (JsonElement classMappingElement : mappings) {
                    if (!classMappingElement.isJsonObject())
                        continue;

                    JsonObject classMapping = classMappingElement.getAsJsonObject();
                    String className = ensureFieldExists(classMapping, "type").getAsString();
                    Class<?> clazz;

                    try {
                        clazz = Class.forName(className);
                    } catch (ClassNotFoundException error) {
                        throw new MappingFormatException("Invalid class name: " + className);
                    }

                    Map<String, String> classMappings = new HashMap<>();

                    JsonElement jsonMappingsElement = ensureFieldExists(classMapping, "mappings");
                    if (!jsonMappingsElement.isJsonObject())
                        throw new MappingFormatException("Mapping for class '" + className + "' is not an object");

                    JsonObject jsonMappings = jsonMappingsElement.getAsJsonObject();
                    jsonMappings.entrySet().forEach(entry -> {
                        if (!entry.getValue().isJsonPrimitive()) {
                            Log.warn("Mapping entry is not string: " + entry.getKey() + ", skipping...");
                            return;
                        }
                        classMappings.put(entry.getKey(), entry.getValue().getAsString());
                    });

                    if (!classMappings.isEmpty())
                        mappedNames.put(clazz, classMappings);
                }

            }

        } catch (Exception error) {
            Log.error(error, "Failed to fetch minecraft names mapper:");
        }

        return mappedNames.isEmpty() ? Collections.emptyMap() : mappedNames;
    }

    private static JsonElement ensureFieldExists(JsonObject object, String fieldName) throws MappingFormatException {
        JsonElement value = object.get(fieldName);
        if (value != null)
            return value;
        throw new MappingFormatException("Missing field: " + fieldName);
    }

    private static class MappingFormatException extends IllegalArgumentException {

        MappingFormatException(String message) {
            super(message);
        }

    }

}
