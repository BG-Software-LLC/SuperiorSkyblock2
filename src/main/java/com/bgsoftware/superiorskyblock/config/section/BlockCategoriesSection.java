package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.block.BlockCategory;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
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
import com.bgsoftware.superiorskyblock.world.block.BlockCategoryImpl;
import com.bgsoftware.superiorskyblock.world.block.BuiltinBlockCategory;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BlockCategoriesSection implements SettingsManager.BlockCategories {

    public static final String[] IGNORED_SECTIONS = new String[]{"custom-categories", "builtin-categories.ALL.actions"};

    private final Map<String, BlockCategory> nameToCategory;
    private final KeyMap<List<BlockCategory>> blockToCategory;

    private final LazyReference<List<BlockCategory>> blockCategories = new LazyReference<List<BlockCategory>>() {
        @Override
        protected List<BlockCategory> create() {
            return nameToCategory.isEmpty() ? Collections.emptyList() :
                    Collections.unmodifiableList(new LinkedList<>(nameToCategory.values()));
        }
    };

    public BlockCategoriesSection(YamlConfiguration cfg) {
        this.nameToCategory = loadInternal(cfg);
        this.blockToCategory = convertBlockToCategoryInternal(this.nameToCategory.values());
    }

    public static void removeInvalidBlocks(YamlConfiguration config, File file) {
        if (!config.isConfigurationSection("custom-categories")) {
            return;
        }

        boolean removed = false;
        for (String categoryName : config.getConfigurationSection("custom-categories").getKeys(false)) {
            List<String> blocks = config.getStringList("custom-categories." + categoryName + ".blocks");

            Iterator<String> iterator = blocks.iterator();
            while (iterator.hasNext()) {
                Material material = EnumHelper.getEnum(Material.class, iterator.next());
                if (material == null) {
                    iterator.remove();
                    removed = true;
                }
            }

            if (blocks.isEmpty()) {
                config.set("custom-categories." + categoryName, null);
            } else {
                config.set("custom-categories." + categoryName + ".blocks", blocks);
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

    private static Map<String, BlockCategory> loadInternal(YamlConfiguration config) {
        Map<String, BlockCategory> blockCategories = new HashMap<>();

        loadCategories(config, "builtin-categories", blockCategories, true);
        loadCategories(config, "custom-categories", blockCategories, false);

        return blockCategories.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(blockCategories);
    }

    private static void loadCategories(YamlConfiguration config, String path, Map<String, BlockCategory> blockCategories, boolean builtin) {
        ConfigurationSection categoriesSection = config.getConfigurationSection(path);
        if (categoriesSection == null) {
            return;
        }

        for (String categoryName : categoriesSection.getKeys(false)) {
            String key = categoryName.toLowerCase(Locale.ENGLISH);

            if (blockCategories.containsKey(key)) {
                Log.warnFromFile("block-categories.yml", "Duplicate block category ", categoryName, ", skipping...");
                continue;
            }

            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

            if (categorySection == null) {
                continue;
            }

            KeySet blocks;
            if (builtin) {
                BuiltinBlockCategory builtinBlockCategory;
                try {
                    builtinBlockCategory = BuiltinBlockCategory.valueOf(categoryName.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException e) {
                    Log.warnFromFile("block-categories.yml", "Invalid builtin category ", categoryName, ", skipping...");
                    continue;
                }

                blocks = KeySets.createHashSet(KeyIndicator.MATERIAL);
                blocks.addAll(builtinBlockCategory.getBlocks());

                if (categorySection.isList("skipped-blocks")) {
                    KeySet skippedBlocks = KeySets.createHashSet(KeyIndicator.MATERIAL, categorySection.getStringList("skipped-blocks"));
                    blocks.removeAll(skippedBlocks);
                }
            } else {
                blocks = KeySets.createHashSet(KeyIndicator.MATERIAL, categorySection.getStringList("blocks"));
            }

            IslandPrivilege placePrivilege = getOrRegisterPrivilege(categorySection.getString("actions.PLACE"));
            IslandPrivilege breakPrivilege = getOrRegisterPrivilege(categorySection.getString("actions.BREAK"));
            IslandPrivilege interactPrivilege = getOrRegisterPrivilege(categorySection.getString("actions.INTERACT"));

            blockCategories.put(key, new BlockCategoryImpl(categoryName, blocks, placePrivilege, breakPrivilege, interactPrivilege));
        }
    }

    private static KeyMap<List<BlockCategory>> convertBlockToCategoryInternal(Collection<BlockCategory> blockCategories) {
        KeyMap<List<BlockCategory>> categories = KeyMaps.createHashMap(KeyIndicator.MATERIAL);

        for (BlockCategory blockCategory : blockCategories) {
            for (Key key : blockCategory.getBlocks()) {
                categories.computeIfAbsent(key, k -> new LinkedList<>()).add(blockCategory);
            }
        }

        if (categories.isEmpty()) {
            return KeyMaps.createEmptyMap();
        }

        // Convert keyMap to unmodifiable.
        KeyMap<List<BlockCategory>> categoriesUnmodifiable = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
        categories.forEach((key, value) -> categoriesUnmodifiable.put(key, Collections.unmodifiableList(value)));
        return KeyMaps.unmodifiableKeyMap(categoriesUnmodifiable);
    }

    @Override
    public List<BlockCategory> getCategories() {
        return this.blockCategories.get();
    }

    @Override
    public List<BlockCategory> getCategories(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");

        return this.blockToCategory.getOrDefault(key, Collections.emptyList());
    }

    @Override
    @Nullable
    public BlockCategory getCategoryByName(String name) {
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

}
