package com.bgsoftware.superiorskyblock.player.cache;

import com.bgsoftware.superiorskyblock.api.player.cache.PlayerCache;
import com.bgsoftware.superiorskyblock.api.player.cache.PlayerCacheKey;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.BaseCacheImpl;

import java.util.function.Function;

public class PlayerCacheImpl implements PlayerCache {

    private final BaseCacheImpl<PlayerCacheKey<?>> cache = new BaseCacheImpl<>(PlayerCacheKey.values());

    private final SuperiorPlayer superiorPlayer;

    public PlayerCacheImpl(SuperiorPlayer superiorPlayer) {
        this.superiorPlayer = superiorPlayer;
    }

    @Override
    public SuperiorPlayer getPlayer() {
        return this.superiorPlayer;
    }

    @Override
    public <T> T store(PlayerCacheKey<T> key, T value) {
        return this.cache.store(key, value);
    }

    @Override
    public <T> T remove(PlayerCacheKey<T> key) {
        return this.cache.remove(key);
    }

    @Override
    public <T> T get(PlayerCacheKey<T> key) {
        return this.cache.get(key);
    }

    @Override
    public <T> T getOrDefault(PlayerCacheKey<T> key, T def) {
        return this.cache.getOrDefault(key, def);
    }

    @Override
    public <T> T computeIfAbsent(PlayerCacheKey<T> key, Function<PlayerCacheKey<T>, T> mappingFunction) {
        return this.cache.computeIfAbsent(key, unused -> mappingFunction.apply(key));
    }

}
