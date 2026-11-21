package com.bgsoftware.superiorskyblock.core.key.category;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.core.value.IntValue;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Thread-safe holder of limits of an island, keyed by {@link KeyCategoryId}.
 */
public class KeyCategoryLimits {

    private final Synchronized<EnumerateMap<KeyCategoryId, IntValue>> limits =
            Synchronized.of(new EnumerateMap<>(KeyCategoryId.values()));

    @Nullable
    public IntValue get(KeyCategoryId id) {
        return this.limits.readAndGet(map -> map.get(id));
    }

    @Nullable
    public IntValue put(KeyCategoryId id, IntValue value) {
        return this.limits.writeAndGet(map -> map.put(id, value));
    }

    @Nullable
    public IntValue remove(KeyCategoryId id) {
        // EnumerateMap#remove decreases the size even if there was no value, so check first.
        return this.limits.writeAndGet(map -> map.containsKey(id) ? map.remove(id) : null);
    }

    public void clear() {
        this.limits.write(EnumerateMap::clear);
    }

    public boolean isEmpty() {
        return this.limits.readAndGet(EnumerateMap::isEmpty);
    }

    public void putAll(KeyCategoryLimits other) {
        List<Map.Entry<KeyCategoryId, IntValue>> entries = new ArrayList<>();
        other.forEach((id, value) -> entries.add(new AbstractMap.SimpleEntry<>(id, value)));
        this.limits.write(map -> {
            for (Map.Entry<KeyCategoryId, IntValue> entry : entries)
                map.put(entry.getKey(), entry.getValue());
        });
    }

    public void removeIf(Predicate<IntValue> predicate) {
        this.limits.write(map -> map.values().removeIf(predicate));
    }

    public void forEach(BiConsumer<KeyCategoryId, IntValue> consumer) {
        this.limits.read(map -> {
            for (KeyCategoryId id : new ArrayList<>(KeyCategoryId.values())) {
                IntValue value = map.get(id);
                if (value != null)
                    consumer.accept(id, value);
            }
        });
    }

    /**
     * Convert the limits into a map of categories.
     *
     * @param mapper     The categories mapper to get the categories from.
     * @param customOnly Whether to include only values that are not synced.
     */
    public Map<KeyCategory, Integer> toCategoriesMap(SettingsManager.CategoriesMapper mapper, boolean customOnly) {
        return this.limits.readAndGet(map -> {
            if (map.isEmpty())
                return Collections.<KeyCategory, Integer>emptyMap();

            Map<KeyCategory, Integer> result = new HashMap<>();

            for (KeyCategory category : mapper.getCategories()) {
                KeyCategoryId id = KeyCategoryId.of(category);
                if (id == null)
                    continue;

                IntValue value = map.get(id);
                if (value != null && (!customOnly || !value.isSynced()))
                    result.put(category, value.get());
            }

            return result.isEmpty() ? Collections.<KeyCategory, Integer>emptyMap() :
                    Collections.unmodifiableMap(result);
        });
    }

}
