package com.bgsoftware.superiorskyblock.island.cache;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.cache.IslandCache;
import com.bgsoftware.superiorskyblock.api.island.cache.IslandCacheKey;
import com.bgsoftware.superiorskyblock.core.BaseCacheImpl;

import java.util.function.Function;

public class IslandCacheImpl implements IslandCache {

    private final BaseCacheImpl<IslandCacheKey<?>> cache = new BaseCacheImpl<>(IslandCacheKey.values());

    private final Island island;

    public IslandCacheImpl(Island island) {
        this.island = island;
    }

    @Override
    public Island getIsland() {
        return this.island;
    }

    @Override
    public <T> T store(IslandCacheKey<T> key, T value) {
        return this.cache.store(key, value);
    }

    @Override
    public <T> T remove(IslandCacheKey<T> key) {
        return this.cache.remove(key);
    }

    @Override
    public <T> T get(IslandCacheKey<T> key) {
        return this.cache.get(key);
    }

    @Override
    public <T> T getOrDefault(IslandCacheKey<T> key, T def) {
        return this.cache.getOrDefault(key, def);
    }

    @Override
    public <T> T computeIfAbsent(IslandCacheKey<T> key, Function<IslandCacheKey<T>, T> mappingFunction) {
        return this.cache.computeIfAbsent(key, unused -> mappingFunction.apply(key));
    }

}
