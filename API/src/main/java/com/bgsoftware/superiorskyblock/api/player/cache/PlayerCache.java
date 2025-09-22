package com.bgsoftware.superiorskyblock.api.player.cache;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.function.Function;

/**
 * Player caches can be used by other plugins to store temporary data for a player, retrieve it, etc.
 * The cache is not persistent between server sessions. For persistent data solution, see {@link SuperiorPlayer#getPersistentDataContainer()}
 * Data can be stored and retrieved by registering custom {@link PlayerCacheKey} for your plugin.
 * The cache is thread-safe and can be accessed from multiple threads.
 */
public interface PlayerCache {

    /**
     * Get the player this cache is for.
     */
    SuperiorPlayer getPlayer();

    /**
     * Store data in this cache.
     *
     * @param key   The key to store.
     * @param value The value to store.
     * @return The old value that was stored in the cache, or null if not data was cached.
     */
    @Nullable
    <T> T store(PlayerCacheKey<T> key, T value);

    /**
     * Remove data from this cache.
     *
     * @param key The cache key.
     * @return The old value that was stored in the cache, or null if not data was cached.
     */
    @Nullable
    <T> T remove(PlayerCacheKey<T> key);

    /**
     * Get data that was stored.
     *
     * @param key The cache key
     * @return The value stored for the provided key, or null if no data was cached.
     */
    @Nullable
    <T> T get(PlayerCacheKey<T> key);

    /**
     * Get data that was stored.
     *
     * @param key The cache key
     * @param def The value to return in case the cache did not contain the provided key
     * @return The value stored for the provided key.
     */
    <T> T getOrDefault(PlayerCacheKey<T> key, T def);

    /**
     * Get data that was stored.
     *
     * @param key             The cache key
     * @param mappingFunction The value to store in case the cache did not contain the provided key
     * @return The value stored for the provided key.
     */
    <T> T computeIfAbsent(PlayerCacheKey<T> key, Function<PlayerCacheKey<T>, T> mappingFunction);

}
