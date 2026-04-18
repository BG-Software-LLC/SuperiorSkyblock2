package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.world.entity.EntityCategoryImpl;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EntityCategoriesSection implements SettingsManager.EntityCategories {

    @Nullable
    private static final Class<?> HOGLIN_CLASS = getEntityTypeClass("org.bukkit.entity.Hoglin");
    @Nullable
    private static final Class<?> SKELETON_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.SkeletonHorse");
    @Nullable
    private static final Class<?> ZOMBIE_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.ZombieHorse");
    @Nullable
    private static final Class<?> ZOMBIE_NAUTILUS_CLASS = getEntityTypeClass("org.bukkit.entity.ZombieNautilus");

    private static final KeySet TAMEABLE_ENTITY_KEYS = createTameableEntityKeys();
    private static final KeySet ANIMAL_ENTITY_KEYS = createAnimalEntityKeys();
    private static final KeySet MONSTER_ENTITY_KEYS = createMonsterEntityKeys();

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

    private static Map<String, EntityCategory> loadInternal(YamlConfiguration cfg) {
        Map<String, EntityCategory> entityCategories = new HashMap<>();
        for (String categoryName : cfg.getKeys(false)) {
            String key = categoryName.toLowerCase(Locale.ENGLISH);

            if (entityCategories.containsKey(categoryName)) {
                Log.warnFromFile("entity-categories.yml", "Duplicate entity category ", categoryName, " - skipping...");
                continue;
            }

            ConfigurationSection section = cfg.getConfigurationSection(categoryName);

            KeySet entities;
            if (categoryName.equalsIgnoreCase("TAMEABLE")) {
                entities = TAMEABLE_ENTITY_KEYS;
            } else if (categoryName.equalsIgnoreCase("ANIMAL")) {
                entities = ANIMAL_ENTITY_KEYS;
            } else if (categoryName.equalsIgnoreCase("MONSTER")) {
                entities = MONSTER_ENTITY_KEYS;
            } else {
                entities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE, section.getStringList("entities"));
            }

            IslandPrivilege spawnPrivilege = getOrRegisterPrivilege(section.getString("actions.SPAWN"));
            IslandPrivilege damagePrivilege = getOrRegisterPrivilege(section.getString("actions.DAMAGE"));
            IslandPrivilege interactPrivilege = getOrRegisterPrivilege(section.getString("actions.INTERACT"));
            IslandFlag spawnerSpawnFlag = getOrRegisterFlag(section.getString("actions.SPAWNER_SPAWN"));
            IslandFlag naturalSpawnFlag = getOrRegisterFlag(section.getString("actions.NATURAL_SPAWN"));

            entityCategories.put(key, new EntityCategoryImpl(categoryName, entities, spawnPrivilege, damagePrivilege,
                    interactPrivilege, spawnerSpawnFlag, naturalSpawnFlag));
        }
        return entityCategories.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(entityCategories);
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

    private static KeySet createTameableEntityKeys() {
        KeySet tameableEntities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
        for (EntityType entityType : EntityType.values()) {
            if (entityType.getEntityClass() != null && Tameable.class.isAssignableFrom(entityType.getEntityClass())) {
                tameableEntities.add(Keys.of(entityType));
            }
        }
        return tameableEntities;
    }

    private static KeySet createAnimalEntityKeys() {
        KeySet animalEntities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
        for (EntityType entityType : EntityType.values()) {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            if (entityClass != null && !isMonsterType(entityClass) &&
                    (Creature.class.isAssignableFrom(entityClass) || Ambient.class.isAssignableFrom(entityClass))) {
                animalEntities.add(Keys.of(entityType));
            }
        }
        return animalEntities;
    }

    private static boolean isMonsterType(Class<? extends Entity> entityClass) {
        return Monster.class.isAssignableFrom(entityClass) ||
                Slime.class.isAssignableFrom(entityClass) || Flying.class.isAssignableFrom(entityClass) ||
                entityClass == HOGLIN_CLASS || entityClass == SKELETON_HORSE_CLASS ||
                entityClass == ZOMBIE_HORSE_CLASS || entityClass == ZOMBIE_NAUTILUS_CLASS;
    }

    private static KeySet createMonsterEntityKeys() {
        KeySet monsterEntities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
        for (EntityType entityType : EntityType.values()) {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            if (entityClass != null && isMonsterType(entityClass)) {
                monsterEntities.add(Keys.of(entityType));
            }
        }
        return monsterEntities;
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

    @Nullable
    private static Class<?> getEntityTypeClass(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException error) {
            return null;
        }
    }

}
