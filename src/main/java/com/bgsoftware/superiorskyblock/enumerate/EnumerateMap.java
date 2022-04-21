package com.bgsoftware.superiorskyblock.enumerate;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class EnumerateMap<K extends Enumerable, V> {

    private final ArrayList<V> values;

    private int size = 0;

    public EnumerateMap(Collection<K> enumerables) {
        this.values = new ArrayList<>(enumerables.size());
    }

    public EnumerateMap(EnumerateMap<K, V> other) {
        this.values = new ArrayList<>(other.values.size());
        this.values.addAll(other.values);
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
        return isValidKey(key) ? this.values.get(key.ordinal()) : null;
    }

    public V getOrDefault(K key, V def) {
        V value = get(key);
        return value == null ? def : value;
    }

    @Nullable
    public V put(K key, V value) {
        if (!isValidKey(key)) {
            // Invalid key
            if (key.ordinal() < 0)
                return null;

            this.values.ensureCapacity(key.ordinal() + 1);
        }

        V oldValue = this.values.set(key.ordinal(), value);

        if (oldValue == null)
            ++this.size;

        return oldValue;
    }

    @Nullable
    public V remove(K key) {
        if (!isValidKey(key))
            return null;

        V oldValue = this.values.remove(key.ordinal());
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
        this.values.clear();
        this.size = 0;
    }

    private boolean isValidKey(K key) {
        return key.ordinal() >= 0 && key.ordinal() < this.values.size();
    }

}
