package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.formatting.IBiFormatter;
import com.bgsoftware.superiorskyblock.core.key.MaterialKeySource;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.bgsoftware.superiorskyblock.core.key.types.SpawnerKey;
import com.bgsoftware.superiorskyblock.core.messages.MinecraftTranslations;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Locale;
import java.util.Optional;

public class KeyNameFormatter implements IBiFormatter<Key, Locale> {

    private static final KeyNameFormatter INSTANCE = new KeyNameFormatter();

    private static final String SPAWNER_FORMAT_KEY = "format.spawner";
    private static final String DEFAULT_SPAWNER_FORMAT = "{0} ({1})";

    public static KeyNameFormatter getInstance() {
        return INSTANCE;
    }

    private KeyNameFormatter() {

    }

    @Override
    public String format(Key key, Locale locale) {
        if (key == null)
            return "";

        key = unwrapKey(key);

        String keyName = key.toString();
        if (Text.isBlank(keyName))
            return "";

        return getTranslatedName(key, locale).orElseGet(() -> Formatters.CAPITALIZED_FORMATTER.format(keyName));
    }

    public Optional<String> getTranslatedName(Key key, Locale locale) {
        // Translations are never loaded on legacy versions, therefore no 1.13+ API is called on them.
        if (key == null || locale == null || !MinecraftTranslations.hasTranslations(locale))
            return Optional.empty();

        return translate(unwrapKey(key), locale);
    }

    @Nullable
    private static String getMinecraftName(Object type, @Nullable String fallbackName) {
        // Compiled against legacy API, therefore Keyed is checked dynamically.
        if (type instanceof Keyed)
            return ((Keyed) type).getKey().getKey();
        return fallbackName == null ? null : fallbackName.toLowerCase(Locale.ENGLISH);
    }

    private static Key unwrapKey(Key key) {
        while (key instanceof LazyKey)
            key = ((LazyKey<?>) key).getBaseKey();
        return key;
    }

    private Optional<String> translate(Key key, Locale locale) {
        // Overrides are checked first, allowing to name custom keys and change vanilla names.
        Optional<String> overrideName = getOverrideName(key.toString(), locale);
        if (overrideName.isPresent())
            return overrideName;

        if (key instanceof SpawnerKey)
            return translateSpawner((SpawnerKey) key, locale);

        if (!Text.isBlank(key.getSubKey())) {
            overrideName = getOverrideName(key.getGlobalKey(), locale);
            if (overrideName.isPresent())
                return overrideName;
        }

        if (key instanceof MaterialKey) {
            Material material = ((MaterialKey) key).getMaterial();
            // Legacy materials have no minecraft keys.
            if (material.name().startsWith("LEGACY_"))
                return Optional.empty();

            String name = getMinecraftName(material, material.name());
            String blockKey = "block.minecraft." + name;
            String itemKey = "item.minecraft." + name;
            boolean isBlock = ((MaterialKey) key).getMaterialKeySource() == MaterialKeySource.BLOCK;
            Optional<String> materialName = MinecraftTranslations.translate(isBlock ? blockKey : itemKey, locale);
            return materialName.isPresent() ? materialName : MinecraftTranslations.translate(isBlock ? itemKey : blockKey, locale);
        }

        if (key instanceof EntityTypeKey) {
            EntityType entityType = ((EntityTypeKey) key).getEntityType();
            // Unknown entity type has no minecraft key.
            if (entityType == EntityType.UNKNOWN)
                return Optional.empty();

            // EntityType is not Keyed in 1.13, but its name is the same as its minecraft key.
            String name = getMinecraftName(entityType, entityType.getName());
            return name == null ? Optional.empty() : MinecraftTranslations.translate("entity.minecraft." + name, locale);
        }

        // Custom keys (custom blocks, custom entities, etc.) can only be named using overrides.
        // We do not try to guess vanilla names for them, as their names may collide with vanilla names.
        return Optional.empty();
    }

    private Optional<String> translateSpawner(SpawnerKey spawnerKey, Locale locale) {
        // The override of the global spawner key is used only as the base spawner name,
        // so it doesn't replace the names of all spawner types.
        Optional<String> spawnerName = getOverrideName(spawnerKey.getGlobalKey(), locale);
        if (!spawnerName.isPresent())
            spawnerName = MinecraftTranslations.translate("block.minecraft.spawner", locale);

        Key spawnerTypeKey = spawnerKey.getSpawnerTypeKey();
        if (!spawnerName.isPresent() || spawnerTypeKey == null)
            return spawnerName;

        String spawnerFormat = getOverrideName(SPAWNER_FORMAT_KEY, locale).orElse(DEFAULT_SPAWNER_FORMAT);
        return Optional.of(spawnerFormat
                .replace("{0}", spawnerName.get())
                .replace("{1}", format(spawnerTypeKey, locale)));
    }

    private static Optional<String> getOverrideName(String key, Locale locale) {
        return MinecraftTranslations.translate(MinecraftTranslations.OVERRIDE_KEY_PREFIX + key, locale);
    }

}
