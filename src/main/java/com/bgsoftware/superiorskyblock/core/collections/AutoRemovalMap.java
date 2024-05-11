package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class AutoRemovalMap<K, V> implements Map<K, V> {

    private static final Object DUMMY = new Object();

    private final Map<K, V> elements;
    private final Cache<K, Object> elementsLifeTime;

    public static <K, V> AutoRemovalMap<K, V> newHashMap(long removalDelay, TimeUnit timeUnit) {
        return new AutoRemovalMap<>(removalDelay, timeUnit, HashMap::new);
    }

    private AutoRemovalMap(long removalDelay, TimeUnit timeUnit, Supplier<Map<K, V>> mapSupplier) {
        this.elements = mapSupplier.get();
        this.elementsLifeTime = CacheBuilder.newBuilder()
                .expireAfterWrite(removalDelay, timeUnit)
                .removalListener(removalNotification -> {
                    elements.remove(removalNotification.getKey());
                })
                .build();
    }

    @Override
    public int size() {
        refreshLifeTime();
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        refreshLifeTime();
        return elements.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        refreshLifeTime(key);
        return elements.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        refreshLifeTime();
        return elements.containsValue(value);
    }

    @Override
    public V get(Object key) {
        refreshLifeTime(key);
        return elements.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        refreshLifeTime(key);
        V old = elements.put(key, value);
        if (old != value)
            this.elementsLifeTime.put(key, DUMMY);
        return value;
    }

    @Override
    public V remove(Object key) {
        V value = elements.remove(key);
        this.elementsLifeTime.invalidate(key);
        return value;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.elementsLifeTime.invalidateAll();
        elements.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        refreshLifeTime();
        return elements.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        refreshLifeTime();
        return elements.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        refreshLifeTime();
        return elements.entrySet();
    }

    private void refreshLifeTime(Object o) {
        try {
            this.elementsLifeTime.get((K) o, () -> null);
        } catch (Throwable ignored) {

        }
    }

    private void refreshLifeTime() {
        this.elementsLifeTime.size();
    }

}
