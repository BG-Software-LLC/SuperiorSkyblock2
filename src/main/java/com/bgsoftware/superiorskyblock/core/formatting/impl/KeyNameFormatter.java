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
import org.bukkit.entity.EntityType;

import java.util.Locale;
import java.util.Optional;

public class KeyNameFormatter implements IBiFormatter<Key, Locale> {

    private static final KeyNameFormatter INSTANCE = new KeyNameFormatter();

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
        Optional<String> overrideName = MinecraftTranslations.translate(MinecraftTranslations.OVERRIDE_KEY_PREFIX + key, locale);
        if (overrideName.isPresent())
            return overrideName;

        if (!Text.isBlank(key.getSubKey())) {
            overrideName = MinecraftTranslations.translate(MinecraftTranslations.OVERRIDE_KEY_PREFIX + key.getGlobalKey(), locale);
            if (overrideName.isPresent())
                return overrideName;
        }

        if (key instanceof SpawnerKey) {
            Optional<String> spawnerName = MinecraftTranslations.translate("block.minecraft.spawner", locale);
            Key spawnerTypeKey = ((SpawnerKey) key).getSpawnerTypeKey();
            if (!spawnerName.isPresent() || spawnerTypeKey == null)
                return spawnerName;
            return Optional.of(spawnerName.get() + " (" + format(spawnerTypeKey, locale) + ")");
        }

        if (key instanceof MaterialKey) {
            MaterialKey materialKey = (MaterialKey) key;
            String name = getMinecraftName(materialKey.getMaterial(), materialKey.getMaterial().name());
            String blockKey = "block.minecraft." + name;
            String itemKey = "item.minecraft." + name;
            boolean isBlock = materialKey.getMaterialKeySource() == MaterialKeySource.BLOCK;
            Optional<String> materialName = MinecraftTranslations.translate(isBlock ? blockKey : itemKey, locale);
            return materialName.isPresent() ? materialName : MinecraftTranslations.translate(isBlock ? itemKey : blockKey, locale);
        }

        if (key instanceof EntityTypeKey) {
            EntityType entityType = ((EntityTypeKey) key).getEntityType();
            // EntityType is not Keyed in 1.13, but its name is the same as its minecraft key.
            String name = getMinecraftName(entityType, entityType.getName());
            return name == null ? Optional.empty() : MinecraftTranslations.translate("entity.minecraft." + name, locale);
        }

        // Custom keys (custom blocks, custom entities, etc.) can only be named using overrides.
        // We do not try to guess vanilla names for them, as their names may collide with vanilla names.
        return Optional.empty();
    }

}
