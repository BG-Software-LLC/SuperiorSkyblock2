package com.bgsoftware.superiorskyblock.api.island.cache;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IslandCacheKey<T> implements Enumerable {

    private static final Map<String, IslandCacheKey<?>> islandCacheKeys = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final Class<T> valueType;
    private final int ordinal;

    private IslandCacheKey(String name, Class<T> valueType) {
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.valueType = valueType;
        this.ordinal = ordinalCounter++;
    }

    /**
     * Get the name of the island cache key.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of the value for this cache-key.
     */
    public Class<T> getValueType() {
        return valueType;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    @Override
    public String toString() {
        return "IslandCacheKey{name=" + name + "}";
    }

    /**
     * Get all the island cache keys.
     */
    public static Collection<IslandCacheKey<?>> values() {
        return islandCacheKeys.values();
    }

    /**
     * Get an island cache key by its name.
     *
     * @param name The name to check.
     */
    public static IslandCacheKey<?> getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        IslandCacheKey<?> islandCacheKey = islandCacheKeys.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkNotNull(islandCacheKey, "Couldn't find an IslandCacheKey with the name " + name + ".");

        return islandCacheKey;
    }

    /**
     * Register a new island cache key.
     *
     * @param name The name for the island cache key.
     */
    public static <T> void register(String name, Class<T> valueType) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        name = name.toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!islandCacheKeys.containsKey(name), "IslandCacheKey with the name " + name + " already exists.");

        islandCacheKeys.put(name, new IslandCacheKey<>(name, valueType));
    }

}
