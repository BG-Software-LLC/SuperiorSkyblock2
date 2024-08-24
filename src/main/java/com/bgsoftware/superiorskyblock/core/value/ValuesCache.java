package com.bgsoftware.superiorskyblock.core.value;

import java.util.function.IntFunction;

public class ValuesCache<T> {

    private static final int BASE_SIZE = 64;

    private final int minimumCacheValue;
    private final IntFunction<T> creator;
    private final Object[] cache;

    public ValuesCache(IntFunction<T> creator) {
        this(-1, creator);
    }

    public ValuesCache(int minimumCacheValue, IntFunction<T> creator) {
        this.minimumCacheValue = minimumCacheValue;
        this.creator = creator;
        this.cache = new Object[BASE_SIZE - minimumCacheValue];
    }

    public T fetch(int value) {
        int cacheIndex = value - this.minimumCacheValue;
        if (cacheIndex >= 0 && cacheIndex < this.cache.length) {
            Object cachedValue = this.cache[cacheIndex];
            if (cachedValue == null)
                cachedValue = this.cache[cacheIndex] = this.creator.apply(value);

            return (T) cachedValue;
        }

        return this.creator.apply(value);
    }

}
