package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.category.BaseKeyCategory;
import com.bgsoftware.superiorskyblock.core.key.category.BuiltinKeyCategories;
import com.bgsoftware.superiorskyblock.core.key.category.FixedKeyCategory;
import com.bgsoftware.superiorskyblock.core.key.category.KeyCategoryId;
import com.bgsoftware.superiorskyblock.core.key.category.PredicateKeyCategory;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class EntityCategoriesSection extends BaseCategoriesMapper {

    public static final String[] IGNORED_SECTIONS = new String[]{"custom-categories",
            "builtin-categories.TAMED.actions", "builtin-categories.VEHICLE.actions",
            "builtin-categories.MONSTER.actions", "builtin-categories.ANIMAL.actions"};

    public EntityCategoriesSection(YamlConfiguration config) {
        super(config, KeyIndicator.ENTITY_TYPE);

        // Cache all the known values
        for (EntityType entityType : EntityType.values()) {
            getCategories(Keys.of(entityType));
        }

    }

    @Override
    @Nullable
    protected KeyCategory parseCustomCategory(ConfigurationSection section, KeyCategoryId key) {
        KeySet entities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE, section.getStringList("entities"));

        FixedKeyCategory category = new FixedKeyCategory(key.getName(), entities);

        category.setRequirement(KeyCategory.Action.SPAWN, getOrRegisterPrivilege(section.getString("actions.SPAWN")));
        category.setRequirement(KeyCategory.Action.DAMAGE, getOrRegisterPrivilege(section.getString("actions.DAMAGE")));
        category.setRequirement(KeyCategory.Action.INTERACT, getOrRegisterPrivilege(section.getString("actions.INTERACT")));
        category.setRequirement(KeyCategory.Action.SPAWNER_SPAWN, getOrRegisterFlag(section.getString("actions.SPAWNER_SPAWN")));
        category.setRequirement(KeyCategory.Action.NATURAL_SPAWN, getOrRegisterFlag(section.getString("actions.NATURAL_SPAWN")));

        return category;
    }

    @Override
    protected void loadBuiltinCategories(ConfigurationSection section, EnumerateMap<KeyCategoryId, KeyCategory> categories) {
        categories.put(BuiltinKeyCategories.ALL, BuiltinKeyCategories.createAllCategory());

        for (KeyCategoryId key : BuiltinKeyCategories.ENTITY_CATEGORIES) {
            categories.put(key, BuiltinKeyCategories.createEntitiesCategory(key));
        }

        ConfigurationSection categoriesSection = section.getConfigurationSection("builtin-categories");

        if (categoriesSection == null) {
            return;
        }

        for (String categoryName : categoriesSection.getKeys(false)) {
            KeyCategoryId key = KeyCategoryId.getByName(categoryName.toUpperCase(Locale.ENGLISH));

            KeyCategory builtinCategory;
            // In this stage, only built-in categories are in `categories` so we can test against that map.
            if (key == null || (builtinCategory = categories.get(key)) == null) {
                Log.warnFromFile(getFileName(), "Invalid builtin category ", categoryName, ", skipping...");
                continue;
            }

            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

            if (categorySection == null) {
                continue;
            }

            KeySet skippedEntities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE, categorySection.getStringList("skipped-entities"));
            BaseKeyCategory category;

            if (BuiltinKeyCategories.ALL.equals(key)) {
                category = new PredicateKeyCategory(key.getName(), element -> !skippedEntities.contains(element));
            } else {
                KeySet entities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);

                for (Key entity : ((FixedKeyCategory) builtinCategory).getKeys()) {
                    if (!skippedEntities.contains(entity)) {
                        entities.add(entity);
                    }
                }

                category = new FixedKeyCategory(key.getName(), entities);
            }

            category.setRequirement(KeyCategory.Action.SPAWN, getOrRegisterPrivilege(section.getString("actions.SPAWN")));
            category.setRequirement(KeyCategory.Action.DAMAGE, getOrRegisterPrivilege(section.getString("actions.DAMAGE")));
            category.setRequirement(KeyCategory.Action.INTERACT, getOrRegisterPrivilege(section.getString("actions.INTERACT")));
            category.setRequirement(KeyCategory.Action.SPAWNER_SPAWN, getOrRegisterFlag(section.getString("actions.SPAWNER_SPAWN")));
            category.setRequirement(KeyCategory.Action.NATURAL_SPAWN, getOrRegisterFlag(section.getString("actions.NATURAL_SPAWN")));

            categories.put(key, category);
        }
    }

    @Override
    protected String getFileName() {
        return "entity-categories.yml";
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

            if (categoryName.equalsIgnoreCase("VEHICLE")
                    || categoryName.equalsIgnoreCase("MONSTER") || categoryName.equalsIgnoreCase("ANIMAL")) {
                config.set("builtin-categories." + categoryName, categorySection);
            } else if (categoryName.equalsIgnoreCase("TAMEABLE")) {
                config.set("builtin-categories.TAMED", categorySection);
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
                Log.errorFromFile(error, "entity-categories.yml", "An unexpected error occurred while saving file:");
            }
        }
    }

    public static void removeInvalidEntities(YamlConfiguration config, File file) {
        ConfigurationSection categoriesSection = config.getConfigurationSection("custom-categories");

        if (categoriesSection == null) {
            return;
        }

        boolean removed = false;
        for (String categoryName : categoriesSection.getKeys(false)) {
            List<String> entities = categoriesSection.getStringList(categoryName + ".entities");

            Iterator<String> iterator = entities.iterator();
            while (iterator.hasNext()) {
                String entity = iterator.next().split(":")[0];

                if (EnumHelper.getEnum(EntityType.class, entity) == null) {
                    iterator.remove();
                    removed = true;
                }
            }

            if (entities.isEmpty()) {
                categoriesSection.set(categoryName, null);
            } else {
                categoriesSection.set(categoryName + ".entities", entities);
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
