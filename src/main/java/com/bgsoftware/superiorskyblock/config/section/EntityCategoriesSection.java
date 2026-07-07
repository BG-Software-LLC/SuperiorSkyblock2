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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EntityCategoriesSection implements SettingsManager.EntityCategories {

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

    public static void removeInvalidEntityKeys(YamlConfiguration cfg, File file) {
        boolean removed = false;
        for (String categoryName : cfg.getKeys(false)) {
            if (EnumHelper.getEnum(BuiltinEntityCategory.class, categoryName) == null) {
                List<String> entities = cfg.getStringList(categoryName + ".entities");
                Iterator<String> iterator = entities.iterator();
                while (iterator.hasNext()) {
                    EntityType entityType = EnumHelper.getEnum(EntityType.class, iterator.next());
                    if (entityType == null) {
                        iterator.remove();
                        removed = true;
                    }
                }
                if (entities.isEmpty()) {
                    cfg.set(categoryName, null);
                } else {
                    cfg.set(categoryName + ".entities", entities);
                }
            }
        }

        if (removed) {
            try {
                cfg.save(file);
            } catch (IOException ignored) {
            }
        }

    }

    private static Map<String, EntityCategory> loadInternal(YamlConfiguration cfg) {
        Map<String, EntityCategory> entityCategories = new HashMap<>();

        for (String categoryName : cfg.getKeys(false)) {
            String key = categoryName.toLowerCase(Locale.ENGLISH);

            if (entityCategories.containsKey(categoryName)) {
                Log.warnFromFile("entity-categories.yml", "Duplicate entity category ", categoryName, " - skipping...");
                continue;
            }

            ConfigurationSection section = cfg.getConfigurationSection(categoryName);

            BuiltinEntityCategory builtinEntityCategory = EnumHelper.getEnum(BuiltinEntityCategory.class,
                    categoryName.toUpperCase(Locale.ENGLISH));

            KeySet entities = builtinEntityCategory != null ? builtinEntityCategory.getEntities() :
                    KeySets.createHashSet(KeyIndicator.ENTITY_TYPE, section.getStringList("entities"));

            IslandPrivilege spawnPrivilege = getOrRegisterPrivilege(section.getString("actions.SPAWN"));
            IslandPrivilege damagePrivilege = getOrRegisterPrivilege(section.getString("actions.DAMAGE"));
            IslandPrivilege interactPrivilege = getOrRegisterPrivilege(section.getString("actions.INTERACT"));
            IslandFlag spawnerSpawnFlag = getOrRegisterFlag(section.getString("actions.SPAWNER_SPAWN"));
            IslandFlag naturalSpawnFlag = getOrRegisterFlag(section.getString("actions.NATURAL_SPAWN"));

            entityCategories.put(key, new EntityCategoryImpl(categoryName, entities, spawnPrivilege, damagePrivilege,
                    interactPrivilege, spawnerSpawnFlag, naturalSpawnFlag));
        }

        // Add rest of built-in entity categories
        for (BuiltinEntityCategory builtinEntityCategory : BuiltinEntityCategory.values()) {
            if (!entityCategories.containsKey(builtinEntityCategory.name())) {
                entityCategories.put(builtinEntityCategory.name(), new EntityCategoryImpl(builtinEntityCategory.name(),
                        builtinEntityCategory.getEntities(), null, null,
                        null, null, null));
            }
        }

        return Collections.unmodifiableMap(entityCategories);
    }

    private static KeyMap<List<EntityCategory>> convertEntityToCategoryInternal(Collection<EntityCategory> entityCategories) {
        KeyMap<List<EntityCategory>> categories = KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE);
        for (EntityCategory entityCategory : entityCategories) {
            for (Key key : entityCategory.getEntities()) {
                categories.computeIfAbsent(key, k -> new LinkedList<>()).add(entityCategory);
            }
        }
        if (categories.isEmpty())
            return KeyMaps.createEmptyMap();

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
    public EntityCategory getCategoryByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null");
        return this.nameToCategory.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Nullable
    private static IslandPrivilege getOrRegisterPrivilege(@Nullable String name) {
        if (name == null)
            return null;

        try {
            return IslandPrivilege.getByName(name);
        } catch (NullPointerException error) {
            IslandPrivilege.register(name);
            return IslandPrivilege.getByName(name);
        }
    }

    @Nullable
    private static IslandFlag getOrRegisterFlag(@Nullable String name) {
        if (name == null)
            return null;

        try {
            return IslandFlag.getByName(name);
        } catch (NullPointerException error) {
            IslandFlag.register(name);
            return IslandFlag.getByName(name);
        }
    }

}
