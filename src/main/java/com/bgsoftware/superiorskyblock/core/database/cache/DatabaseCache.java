package com.bgsoftware.superiorskyblock.core.database.cache;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class DatabaseCache<V> {

    private final Map<UUID, V> cache = new HashMap<>();

    public DatabaseCache() {

    }

    public V computeIfAbsentInfo(UUID uuid, Supplier<V> value) {
        return cache.computeIfAbsent(uuid, u -> value.get());
    }

    public V computeIfAbsentInfo(UUID uuid, Function<UUID, V> value) {
        return cache.computeIfAbsent(uuid, value);
    }

    @Nullable
    public V getCachedInfo(UUID uuid) {
        return cache.get(uuid);
    }

}
