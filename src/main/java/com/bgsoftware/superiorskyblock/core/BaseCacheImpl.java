package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class BaseCacheImpl<K extends Enumerable> {

    private final Synchronized<EnumerateMap<K, Object>> cache;

    public BaseCacheImpl(Collection<K> keys) {
        this.cache = Synchronized.of(new EnumerateMap<>(keys));
    }

    public final <T> T store(K key, T value) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Preconditions.checkNotNull(value, "value parameter cannot be null");

        Object oldValue = this.cache.writeAndGet(cache -> cache.put(key, value));
        return oldValue == null ? null : (T) oldValue;
    }

    public <T> T remove(K key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");

        Object oldValue = this.cache.writeAndGet(cache -> cache.remove(key));
        return oldValue == null ? null : (T) oldValue;
    }

    public <T> T get(K key) {
        return getOrDefault(key, null);
    }

    public <T> T getOrDefault(K key, T def) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");

        Object oldValue = this.cache.readAndGet(cache -> cache.get(key));
        return oldValue == null ? def : (T) oldValue;
    }

    public <T> T computeIfAbsent(K key, Function<K, T> mappingFunction) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Preconditions.checkNotNull(mappingFunction, "mappingFunction parameter cannot be null");

        return (T) this.cache.writeAndGet(cache -> {
            Object value = cache.get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                cache.put(key, value);
            }
            return value;
        });
    }
}
