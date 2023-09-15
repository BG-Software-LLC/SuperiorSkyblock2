package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * {@link Map} implementation for handling keys.
 * The difference between this map and a regular {@link Map} is that this map handles checks for
 * global keys as well as individual ones.
 * <p>
 * For example, if this map has "STONE" as a key inside it, the {@link #get(Object)} method will return the value
 * of that key when "STONE" is provided, as well as any other key with a different sub-key ("STONE:0", for example).
 * <p>
 * However, if this set has "STONE:0" as a key, as well as the global key inside it, the {@link #get(Object)} method
 * will return the value of the exact same key and not its global key (Therefore, the value of "STONE:0" will be
 * returned if "STONE:0" is provided, and the value of "STONE" will be provided if "STONE:1" is provided)
 */
public interface KeyMap<V> extends Map<Key, V> {

    /**
     * Create a new empty {@link KeyMap<V>} instance.
     *
     * @param mapCreator The map creator for the inner-map of the new {@link KeyMap}
     */
    static <V> KeyMap<V> createKeyMap(Supplier<Map<String, V>> mapCreator) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().createKeyMap(mapCreator);
    }

    /**
     * Create a new empty {@link KeyMap<V>} instance based on {@link HashMap}.
     */
    static <V> KeyMap<V> createKeyMap() {
        return createKeyMap(() -> new HashMap<>());
    }

    /**
     * Create a new {@link KeyMap<V>} instance from the given map based on {@link HashMap}.
     * If the provided map is also a {@link KeyMap<V>}, the exact same instance of that map is returned.
     * Otherwise, the returned {@link KeyMap<V>} is a copy of that map.
     *
     * @param map The map to create {@link KeySet} from.
     */
    static <V> KeyMap<V> createKeyMap(Map<Key, V> map) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().createKeyMap(() -> new HashMap<>(), map);
    }

    /**
     * Create a new empty {@link KeyMap<V>} instance based on {@link ConcurrentHashMap}.
     */
    static <V> KeyMap<V> createConcurrentKeyMap() {
        return createKeyMap(() -> new ConcurrentHashMap<>());
    }

    /**
     * Create a new {@link KeyMap<V>} instance from the given map based on {@link ConcurrentHashMap}.
     * If the provided map is also a {@link KeyMap<V>}, the exact same instance of that map is returned.
     * Otherwise, the returned {@link KeyMap<V>} is a copy of that map.
     *
     * @param map The map to create {@link KeySet} from.
     */
    static <V> KeyMap<V> createConcurrentKeyMap(Map<Key, V> map) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().createKeyMap(() -> new ConcurrentHashMap<>(), map);
    }

    /**
     * Create a collector for {@link KeyMap} that can be used in streams.
     *
     * @param keyMapper   The key mapper to apply.
     * @param valueMapper The values mapper to apply.
     * @param mapCreator  The map creator for the inner-map of the new {@link KeyMap}
     */
    static <K, V> Collector<K, ?, KeyMap<V>> getCollector(Function<? super K, ? extends Key> keyMapper,
                                                          Function<? super K, ? extends V> valueMapper,
                                                          Supplier<Map<String, V>> mapCreator) {
        return Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", v1));
        }, () -> createKeyMap(mapCreator));
    }

    /**
     * Create a collector for {@link KeyMap} based on {@link HashMap} that can be used in streams
     *
     * @param keyMapper   The key mapper to apply.
     * @param valueMapper The values mapper to apply.
     */
    static <K, V> Collector<K, ?, KeyMap<V>> getCollector(Function<? super K, ? extends Key> keyMapper,
                                                          Function<? super K, ? extends V> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", v1));
        }, KeyMap::createKeyMap);
    }

    /**
     * Get the key that is similar to the provided key.
     * For example, if {@param original} is "STONE:0", and the map contains only "STONE", "STONE" will be returned.
     * However, if the map contains "STONE" as well as "STONE:0", "STONE:0" will be returned.
     * If the key is not inside the map, null will be returned.
     *
     * @param original The original key.
     */
    @Nullable
    Key getKey(Key original);

    /**
     * Get the key that is similar to the provided key.
     * For example, if {@param original} is "STONE:0", and the map contains only "STONE", "STONE" will be returned.
     * However, if the map contains "STONE" as well as "STONE:0", "STONE:0" will be returned.
     * If the key is not inside the map, {@param def} will be returned.
     *
     * @param original The original key.
     * @param def      Default key to be returned if {@param original} is not in the map.
     */
    Key getKey(Key original, @Nullable Key def);

    /**
     * Get a value from the key without checking for global keys or other similar keys.
     * This means that if the map doesn't contain {@param key}, {@param def} will be returned.
     * This is a similar behavior to a regular {@link Map}
     *
     * @param key The key to check
     * @param def The default value to return.
     */
    V getRaw(Key key, V def);

    /**
     * See {@link java.util.Collection#removeIf(Predicate)}
     */
    boolean removeIf(Predicate<Key> predicate);

    /**
     * Return a regular {@link java.util.HashMap} with the keys and values of this map.
     */
    Map<Key, V> asMap();

}
