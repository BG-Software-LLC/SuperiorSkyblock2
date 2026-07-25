package com.bgsoftware.superiorskyblock.island.cache;

import com.bgsoftware.superiorskyblock.api.island.cache.IslandCacheKey;
import org.bukkit.Chunk;

import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class IslandCacheKeys {

    public static final IslandCacheKey<Set<Chunk>> PENDING_LOADED_CHUNKS =
            (IslandCacheKey<Set<Chunk>>) (IslandCacheKey) register("PENDING_LOADED_CHUNKS", Set.class);

    private IslandCacheKeys() {

    }

    public static void registerCacheKeys() {
        // Do nothing, only trigger all the register calls
    }

    public static <T> IslandCacheKey<T> register(String name, Class<T> valueType) {
        IslandCacheKey.register(name, valueType);
        return (IslandCacheKey<T>) IslandCacheKey.getByName(name);
    }

}
