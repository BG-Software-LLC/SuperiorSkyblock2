package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.world.entity.BuiltinEntityCategory;
import com.bgsoftware.superiorskyblock.world.entity.EntityCategoryImpl;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EntityCategoriesSection implements SettingsManager.EntityCategories {

    public static final String[] IGNORED_SECTIONS = new String[]{"custom-categories",
            "builtin-categories.TAMEABLE.actions", "builtin-categories.VEHICLE.actions",
            "builtin-categories.MONSTER.actions", "builtin-categories.ANIMAL.actions"};

    private final Map<String, EntityCategory> nameToCategory;
    private final KeyMap<List<EntityCategory>> entityToCategory;

    private final LazyReference<List<EntityCategory>> entityCategories = new LazyReference<List<EntityCategory>>() {
        @Override
        protected List<EntityCategory> create() {
            return nameToCategory.isEmpty() ? Collections.emptyList() :
                    Collections.unmodifiableList(new LinkedList<>(nameToCategory.values()));
        }
    };

    public EntityCategoriesSection(YamlConfiguration cfg) {
        this.nameToCategory = loadInternal(cfg);
        this.entityToCategory = convertEntityToCategoryInternal(this.nameToCategory.values());
    }

    public static void convertToSections(YamlConfiguration config, File file) {
        if (config.isConfigurationSection("builtin-categories")
                && config.isConfigurationSection("custom-categories")) {
            return;
        }

        boolean converted = false;
        for (String categoryName : config.getKeys(false)) {
            if (categoryName.equalsIgnoreCase("builtin-categories")
                    || categoryName.equalsIgnoreCase("custom-categories")) {
                continue;
            }

            ConfigurationSection categorySection = config.getConfigurationSection(categoryName);

            if (categorySection == null) {
                continue;
            }

            if (categoryName.equalsIgnoreCase("TAMEABLE") || categoryName.equalsIgnoreCase("VEHICLE")
                    || categoryName.equalsIgnoreCase("MONSTER") || categoryName.equalsIgnoreCase("ANIMAL")) {
                config.set("builtin-categories." + categoryName, categorySection);
            } else {
                config.set("custom-categories." + categoryName, categorySection);
            }

            config.set(categoryName, null);
            converted = true;
        }

        if (converted) {
            try {
                config.save(file);
            } catch (Exception error) {
                Log.errorFromFile("entity-categories.yml", error, "An unexpected error occurred while saving file:");
            }
        }
    }

    public static void removeInvalidEntities(YamlConfiguration config, File file) {
        if (!config.isConfigurationSection("custom-categories")) {
            return;
        }

        boolean removed = false;
        for (String categoryName : config.getConfigurationSection("custom-categories").getKeys(false)) {
            List<String> entities = config.getStringList("custom-categories." + categoryName + ".entities");

            Iterator<String> iterator = entities.iterator();
            while (iterator.hasNext()) {
                EntityType entityType = EnumHelper.getEnum(EntityType.class, iterator.next());
                if (entityType == null) {
                    iterator.remove();
                    removed = true;
                }
            }

            if (entities.isEmpty()) {
                config.set("custom-categories." + categoryName, null);
            } else {
                config.set("custom-categories." + categoryName + ".entities", entities);
            }
        }

        if (removed) {
            try {
                config.save(file);
            } catch (Exception error) {
                Log.error(error, file, "An unexpected error occurred while saving config file:");
            }
        }
    }

    private static Map<String, EntityCategory> loadInternal(YamlConfiguration config) {
        Map<String, EntityCategory> entityCategories = new HashMap<>();

        loadCategories(config, "builtin-categories", entityCategories, true);
        loadCategories(config, "custom-categories", entityCategories, false);

        return entityCategories.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(entityCategories);
    }

    private static void loadCategories(YamlConfiguration config, String path, Map<String, EntityCategory> entityCategories, boolean builtin) {
        ConfigurationSection categoriesSection = config.getConfigurationSection(path);
        if (categoriesSection == null) {
            return;
        }

        for (String categoryName : categoriesSection.getKeys(false)) {
            String key = categoryName.toLowerCase(Locale.ENGLISH);

            if (entityCategories.containsKey(key)) {
                Log.warnFromFile("entity-categories.yml", "Duplicate entity category ", categoryName, ", skipping...");
                continue;
            }

            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

            if (categorySection == null) {
                continue;
            }

            KeySet entities;
            if (builtin) {
                BuiltinEntityCategory builtinEntityCategory;
                try {
                    builtinEntityCategory = BuiltinEntityCategory.valueOf(categoryName.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException e) {
                    Log.warnFromFile("entity-categories.yml", "Invalid builtin category ", categoryName, ", skipping...");
                    continue;
                }

                entities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
                entities.addAll(builtinEntityCategory.getEntities());

                if (categorySection.isList("skipped-entities")) {
                    KeySet skippedEntities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE, categorySection.getStringList("skipped-entities"));
                    entities.removeAll(skippedEntities);
                }
            } else {
                entities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE, categorySection.getStringList("entities"));
            }

            IslandPrivilege spawnPrivilege = getOrRegisterPrivilege(categorySection.getString("actions.SPAWN"));
            IslandPrivilege damagePrivilege = getOrRegisterPrivilege(categorySection.getString("actions.DAMAGE"));
            IslandPrivilege interactPrivilege = getOrRegisterPrivilege(categorySection.getString("actions.INTERACT"));
            IslandFlag spawnerSpawnFlag = getOrRegisterFlag(categorySection.getString("actions.SPAWNER_SPAWN"));
            IslandFlag naturalSpawnFlag = getOrRegisterFlag(categorySection.getString("actions.NATURAL_SPAWN"));

            entityCategories.put(key, new EntityCategoryImpl(categoryName, entities, spawnPrivilege, damagePrivilege,
                    interactPrivilege, spawnerSpawnFlag, naturalSpawnFlag));
        }
    }

    private static KeyMap<List<EntityCategory>> convertEntityToCategoryInternal(Collection<EntityCategory> entityCategories) {
        KeyMap<List<EntityCategory>> categories = KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE);

        for (EntityCategory entityCategory : entityCategories) {
            for (Key key : entityCategory.getEntities()) {
                categories.computeIfAbsent(key, k -> new LinkedList<>()).add(entityCategory);
            }
        }

        if (categories.isEmpty()) {
            return KeyMaps.createEmptyMap();
        }

        // Convert keyMap to unmodifiable.
        KeyMap<List<EntityCategory>> categoriesUnmodifiable = KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE);
        categories.forEach((key, value) -> categoriesUnmodifiable.put(key, Collections.unmodifiableList(value)));
        return KeyMaps.unmodifiableKeyMap(categoriesUnmodifiable);
    }

    @Override
    public List<EntityCategory> getCategories() {
        return this.entityCategories.get();
    }

    @Override
    public List<EntityCategory> getCategories(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");

        return this.entityToCategory.getOrDefault(key, Collections.emptyList());
    }

    @Override
    @Nullable
    public EntityCategory getCategoryByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null");

        return this.nameToCategory.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Nullable
    private static IslandPrivilege getOrRegisterPrivilege(@Nullable String name) {
        if (name == null) {
            return null;
        }

        try {
            return IslandPrivilege.getByName(name);
        } catch (NullPointerException error) {
            IslandPrivilege.register(name);
            return IslandPrivilege.getByName(name);
        }
    }

    @Nullable
    private static IslandFlag getOrRegisterFlag(@Nullable String name) {
        if (name == null) {
            return null;
        }

        try {
            return IslandFlag.getByName(name);
        } catch (NullPointerException error) {
            IslandFlag.register(name);
            return IslandFlag.getByName(name);
        }
    }

}
