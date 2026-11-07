package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.category.BuiltinKeyCategories;
import com.bgsoftware.superiorskyblock.core.key.category.FixedKeyCategory;
import com.bgsoftware.superiorskyblock.core.key.category.KeyCategoryId;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class BaseCategoriesMapper implements SettingsManager.CategoriesMapper {

    private final EnumerateMap<KeyCategoryId, KeyCategory> categoriesByName;
    private final List<KeyCategory> categoriesList;

    private final KeyMap<TrackedCategories> keyToCategory;
    private final ReadWriteLock keyToCategoryLock = new ReentrantReadWriteLock();

    private final LazyReference<KeyCategory> allCategory = new LazyReference<KeyCategory>() {
        @Override
        protected KeyCategory create() {
            return Objects.requireNonNull(getCategoryByKey(BuiltinKeyCategories.ALL));
        }
    };

    protected BaseCategoriesMapper(YamlConfiguration cfg, KeyIndicator keyIndicator) {
        this.categoriesByName = loadInternal(cfg);
        this.categoriesList = this.categoriesByName.isEmpty() ? Collections.emptyList() :
                Collections.unmodifiableList(new LinkedList<>(categoriesByName.values()));
        this.keyToCategory = convertKeyToCategoryInternal(this.categoriesList, keyIndicator);
    }

    @Override
    public final List<KeyCategory> getCategories() {
        return this.categoriesList;
    }

    @Override
    public final List<KeyCategory> getCategories(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        this.keyToCategoryLock.readLock().lock();
        try {
            TrackedCategories categories = this.keyToCategory.getRaw(key, null);
            if (categories != null && categories.trackedAll) {
                return categories.view;
            }
        } finally {
            this.keyToCategoryLock.readLock().unlock();
        }

        this.keyToCategoryLock.writeLock().lock();
        try {
            TrackedCategories categories = this.keyToCategory.getRaw(key, null);

            if (categories == null) {
                categories = new TrackedCategories();
                Key globalKey = ((BaseKey<?>) key).toGlobalKey();
                TrackedCategories globalCategories = globalKey.equals(key) ? null : this.keyToCategory.getRaw(globalKey, null);
                if (globalCategories != null) {
                    categories.categories.addAll(globalCategories.categories);
                    categories.categories.remove(allCategory.get());
                }
                this.keyToCategory.put(key, categories);
            }

            if (!categories.trackedAll) {
                if (allCategory.get().isListed(key)) {
                    categories.categories.add(allCategory.get());
                }
                categories.trackedAll = true;
            }

            return categories.view;
        } finally {
            this.keyToCategoryLock.writeLock().unlock();
        }
    }

    @Nullable
    public final KeyCategory getCategoryByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null");
        KeyCategoryId key = KeyCategoryId.getByName(name.toUpperCase(Locale.ENGLISH));
        return key == null ? null : getCategoryByKey(key);
    }

    public final KeyCategory getCategoryByKey(KeyCategoryId key) {
        return this.categoriesByName.get(key);
    }

    @Nullable
    protected abstract KeyCategory parseCustomCategory(ConfigurationSection section, KeyCategoryId key);

    protected abstract void loadBuiltinCategories(ConfigurationSection section,
                                                  EnumerateMap<KeyCategoryId, KeyCategory> categories);

    protected abstract String getFileName();


    private KeyMap<TrackedCategories> convertKeyToCategoryInternal(Collection<KeyCategory> categories, KeyIndicator keyIndicator) {
        KeyMap<TrackedCategories> keyToCategory = KeyMaps.createHashMap(keyIndicator);

        for (KeyCategory category : categories) {
            if (category instanceof FixedKeyCategory) {
                for (Key key : ((FixedKeyCategory) category).getKeys()) {
                    TrackedCategories trackedCategories = keyToCategory.getRaw(key, null);
                    if (trackedCategories == null) {
                        trackedCategories = new TrackedCategories();
                        keyToCategory.put(key, trackedCategories);
                    }
                    trackedCategories.categories.add(category);
                }
            }
        }

        keyToCategory.forEach((key, trackedCategories) -> {
            Key globalKey = ((BaseKey<?>) key).toGlobalKey();
            TrackedCategories globalCategories = globalKey.equals(key) ? null : keyToCategory.getRaw(globalKey, null);
            if (globalCategories != null) {
                for (KeyCategory globalCategory : globalCategories.categories) {
                    if (!trackedCategories.categories.contains(globalCategory)) {
                        trackedCategories.categories.add(globalCategory);
                    }
                }
            }
        });

        return keyToCategory;
    }

    private EnumerateMap<KeyCategoryId, KeyCategory> loadInternal(YamlConfiguration config) {
        EnumerateMap<KeyCategoryId, KeyCategory> categories = new EnumerateMap<>(KeyCategoryId.values());

        loadBuiltinCategories(config, categories);
        loadCustomCategories(config, categories);

        return categories;
    }

    private void loadCustomCategories(YamlConfiguration config, EnumerateMap<KeyCategoryId, KeyCategory> categories) {
        ConfigurationSection categoriesSection = config.getConfigurationSection("custom-categories");
        if (categoriesSection == null) {
            return;
        }

        for (String categoryName : categoriesSection.getKeys(false)) {
            String keyName = categoryName.toUpperCase(Locale.ENGLISH);

            KeyCategoryId key = KeyCategoryId.getByName(keyName);

            if (key != null && categories.containsKey(key)) {
                Log.warnFromFile(getFileName(), "Duplicate category ", categoryName, ", skipping...");
                continue;
            }

            if (key == null) {
                key = KeyCategoryId.register(keyName);
            }

            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

            if (categorySection == null) {
                continue;
            }

            KeyCategory category = parseCustomCategory(categorySection, key);
            if (category != null) {
                categories.put(key, category);
            }
        }
    }

    private static class TrackedCategories {

        private final List<KeyCategory> categories = new LinkedList<>();
        private boolean trackedAll = false;
        private final List<KeyCategory> view = Collections.unmodifiableList(categories);

    }

}
