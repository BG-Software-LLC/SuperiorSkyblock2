package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.category.BuiltinKeyCategories;
import com.bgsoftware.superiorskyblock.core.key.category.KeyCategoryId;
import com.bgsoftware.superiorskyblock.core.key.category.FixedKeyCategory;
import com.bgsoftware.superiorskyblock.core.key.category.PredicateKeyCategory;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BlockCategoriesSection extends BaseCategoriesMapper {

    public static final String[] IGNORED_SECTIONS = new String[]{"custom-categories", "builtin-categories.ALL.actions"};

    public BlockCategoriesSection(YamlConfiguration config) {
        super(config, KeyIndicator.MATERIAL);

        // Cache all the known values
        for (Material material : Material.values()) {
            if (material.isBlock()) {
                getCategories(Keys.of(material));
            }
        }

    }

    @Override
    protected KeyCategory parseCustomCategory(ConfigurationSection section, KeyCategoryId key) {
        KeySet blocks = KeySets.createHashSet(KeyIndicator.MATERIAL, section.getStringList("blocks"));

        FixedKeyCategory category = new FixedKeyCategory(key.getName(), blocks);

        category.setRequirement(KeyCategory.Action.PLACE, getOrRegisterPrivilege(section.getString("actions.PLACE")));
        category.setRequirement(KeyCategory.Action.BREAK, getOrRegisterPrivilege(section.getString("actions.BREAK")));
        category.setRequirement(KeyCategory.Action.INTERACT, getOrRegisterPrivilege(section.getString("actions.INTERACT")));

        return category;
    }

    @Override
    protected void loadBuiltinCategories(ConfigurationSection section, EnumerateMap<KeyCategoryId, KeyCategory> categories) {
        categories.put(BuiltinKeyCategories.ALL, BuiltinKeyCategories.createAllCategory());

        ConfigurationSection categoriesSection = section.getConfigurationSection("builtin-categories");

        if (categoriesSection == null) {
            return;
        }

        for (String categoryName : categoriesSection.getKeys(false)) {
            KeyCategoryId key = KeyCategoryId.getByName(categoryName.toUpperCase(Locale.ENGLISH));

            if (!BuiltinKeyCategories.ALL.equals(key)) {
                Log.warnFromFile(getFileName(), "Invalid builtin category ", categoryName, ", skipping...");
                continue;
            }

            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

            if (categorySection == null) {
                continue;
            }

            KeySet skippedBlocks = KeySets.createHashSet(KeyIndicator.MATERIAL, categorySection.getStringList("skipped-blocks"));
            PredicateKeyCategory category = new PredicateKeyCategory(key.getName(),
                    element -> !skippedBlocks.contains(element));

            category.setRequirement(KeyCategory.Action.PLACE,
                    getOrRegisterPrivilege(categorySection.getString("actions.PLACE")));
            category.setRequirement(KeyCategory.Action.BREAK,
                    getOrRegisterPrivilege(categorySection.getString("actions.BREAK")));
            category.setRequirement(KeyCategory.Action.INTERACT,
                    getOrRegisterPrivilege(categorySection.getString("actions.INTERACT")));

            categories.put(key, category);
        }

    }

    @Override
    protected String getFileName() {
        return "block-categories.yml";
    }

    public static void removeInvalidBlocks(YamlConfiguration config, File file) {
        ConfigurationSection categoriesSection = config.getConfigurationSection("custom-categories");

        if (categoriesSection == null) {
            return;
        }

        boolean removed = false;
        for (String categoryName : categoriesSection.getKeys(false)) {
            List<String> blocks = categoriesSection.getStringList(categoryName + ".blocks");

            Iterator<String> iterator = blocks.iterator();
            while (iterator.hasNext()) {
                String block = iterator.next().split(":")[0];

                if (EnumHelper.getEnum(Material.class, block) == null) {
                    iterator.remove();
                    removed = true;
                }
            }

            if (blocks.isEmpty()) {
                categoriesSection.set(categoryName, null);
            } else {
                categoriesSection.set(categoryName + ".blocks", blocks);
            }
        }

        if (removed) {
            try {
                config.save(file);
            } catch (Exception error) {
                Log.error(error, file, "An unexpected error occurred while saving file:");
            }
        }
    }

    public static void warnMissingBlocks(YamlConfiguration config, YamlConfiguration defaultConfig) {
        Set<String> blocks = new HashSet<>();

        ConfigurationSection categoriesSection = config.getConfigurationSection("custom-categories");

        if (categoriesSection != null) {
            for (String categoryName : categoriesSection.getKeys(false)) {
                for (String block : categoriesSection.getStringList(categoryName + ".blocks")) {
                    blocks.add(block.toUpperCase(Locale.ENGLISH));
                }
            }
        }

        ConfigurationSection defaultCategoriesSection = defaultConfig.getConfigurationSection("custom-categories");

        if (defaultCategoriesSection == null) {
            return;
        }

        for (String categoryName : defaultCategoriesSection.getKeys(false)) {
            if (!defaultCategoriesSection.isString(categoryName + ".actions.INTERACT")) {
                continue;
            }

            for (String block : defaultCategoriesSection.getStringList(categoryName + ".blocks")) {
                if (!blocks.contains(block) && EnumHelper.getEnum(Material.class, block) != null) {
                    Log.warn("Potentially missing interactable block ", block);
                }
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

}
