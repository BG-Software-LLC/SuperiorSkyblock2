package com.bgsoftware.superiorskyblock.api.player.cache;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PlayerCacheKey<T> implements Enumerable {

    private static final Map<String, PlayerCacheKey<?>> playerCacheKeys = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final Class<T> valueType;
    private final int ordinal;

    private PlayerCacheKey(String name, Class<T> valueType) {
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.valueType = valueType;
        this.ordinal = ordinalCounter++;
    }

    /**
     * Get the name of the player cache key.
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
        return "PlayerCacheKey{name=" + name + "}";
    }

    /**
     * Get all the player cache keys.
     */
    public static Collection<PlayerCacheKey<?>> values() {
        return playerCacheKeys.values();
    }

    /**
     * Get an player cache key by its name.
     *
     * @param name The name to check.
     */
    public static PlayerCacheKey<?> getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        PlayerCacheKey<?> playerCacheKey = playerCacheKeys.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkNotNull(playerCacheKey, "Couldn't find an PlayerCacheKey with the name " + name + ".");

        return playerCacheKey;
    }

    /**
     * Register a new player cache key.
     *
     * @param name The name for the player cache key.
     */
    public static <T> void register(String name, Class<T> valueType) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        name = name.toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!playerCacheKeys.containsKey(name), "PlayerCacheKey with the name " + name + " already exists.");

        playerCacheKeys.put(name, new PlayerCacheKey<>(name, valueType));
    }

}
