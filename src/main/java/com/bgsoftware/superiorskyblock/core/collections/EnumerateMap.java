package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EnumerateMap<K extends Enumerable, V> {

    private Object[] values;
    private int size = 0;

    public EnumerateMap(Collection<K> enumerables) {
        this.values = new Object[enumerables.size()];
    }

    public EnumerateMap(EnumerateMap<K, V> other) {
        this.values = other.values.clone();
        this.size = other.size;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return size() <= 0;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Nullable
    public V get(K key) {
        return isValidKey(key) ? (V) this.values[key.ordinal()] : null;
    }

    public V getOrDefault(K key, V def) {
        V value = get(key);
        return value == null ? def : value;
    }

    @Nullable
    public V put(K key, @NotNull V value) {
        Preconditions.checkNotNull(value, "Cannot set values as nulls.");

        this.ensureCapacity(key.ordinal() + 1);

        V oldValue = (V) this.values[key.ordinal()];
        this.values[key.ordinal()] = value;

        if (oldValue == null)
            ++this.size;

        return oldValue;
    }

    @Nullable
    public V remove(K key) {
        if (!isValidKey(key))
            return null;

        V oldValue = (V) this.values[key.ordinal()];
        this.values[key.ordinal()] = null;

        --this.size;

        return oldValue;
    }

    public <T> Map<K, T> collect(Collection<K> enumerables, Function<V, T> valuesMapper) {
        Map<K, T> map = new HashMap<>();

        for (K key : enumerables) {
            V value = get(key);
            if (value != null)
                map.put(key, valuesMapper.apply(value));
        }

        return map;
    }

    public void clear() {
        this.values = new Object[this.values.length];
        this.size = 0;
    }

    private boolean isValidKey(K key) {
        return key.ordinal() >= 0 && key.ordinal() < this.values.length;
    }

    private void ensureCapacity(int capacity) {
        if (capacity <= this.values.length)
            return;

        this.values = Arrays.copyOf(this.values, capacity);
    }

}
