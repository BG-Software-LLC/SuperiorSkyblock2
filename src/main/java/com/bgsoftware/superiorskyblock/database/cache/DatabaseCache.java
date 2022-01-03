package com.bgsoftware.superiorskyblock.database.cache;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DatabaseCache<V> {

    private final Map<UUID, V> cache = new HashMap<>();

    public DatabaseCache() {

    }

    public V addCachedInfo(UUID uuid, V value) {
        cache.put(uuid, value);
        return value;
    }

    @Nullable
    public V getCachedInfo(UUID uuid) {
        return cache.get(uuid);
    }

}
