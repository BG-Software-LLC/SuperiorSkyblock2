package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

public class EnumerateMap<K extends Enumerable, V> {

    private Object[] values;
    private int size = 0;

    private Values valuesView;

    public EnumerateMap(Collection<K> enumerables) {
        this.values = new Object[enumerables.size()];
    }

    public EnumerateMap(V[] values) {
        this.values = values.clone();
        for (V value : values)
            if (value != null) ++size;
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

    public void putAll(EnumerateMap<K, V> other) {
        for (int i = 0; i < other.values.length; ++i) {
            Object val = other.values[i];
            if (val != null)
                values[i] = val;
        }
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

    public void clear() {
        this.values = new Object[this.values.length];
        this.size = 0;
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V value = get(key);
        if (value == null) {
            value = mappingFunction.apply(key);
            put(key, value);
        }
        return value;
    }

    public <T> Map<K, T> collect(Collection<K> enumerables, Function<V, T> valuesMapper) {
        Map<K, T> map = new IdentityHashMap<>();

        for (K key : enumerables) {
            V value = get(key);
            if (value != null)
                map.put(key, valuesMapper.apply(value));
        }

        return map;
    }

    public Map<K, V> collect(Collection<K> enumerables) {
        Map<K, V> map = new IdentityHashMap<>();

        for (K key : enumerables) {
            V value = get(key);
            if (value != null)
                map.put(key, value);
        }

        return map;
    }

    public Collection<V> values() {
        if (valuesView == null)
            valuesView = new Values();

        return valuesView;
    }

    private boolean isValidKey(K key) {
        return key.ordinal() >= 0 && key.ordinal() < this.values.length;
    }

    private void ensureCapacity(int capacity) {
        if (capacity <= this.values.length)
            return;

        this.values = Arrays.copyOf(this.values, capacity);
    }

    private class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            return size;
        }

        public boolean contains(Object o) {
            for (Object val : values) {
                if (Objects.equals(o, val))
                    return true;
            }
            return false;
        }

        public boolean remove(Object o) {
            for (int i = 0; i < values.length; i++) {
                if (Objects.equals(o, values[i])) {
                    values[i] = null;
                    size--;
                    return true;
                }
            }

            return false;
        }

        public void clear() {
            EnumerateMap.this.clear();
        }
    }

    private class ValueIterator implements Iterator<V> {

        private int index = 0;

        private int lastReturnedIndex = -1;

        @Override
        public boolean hasNext() {
            while (index < values.length && values[index] == null)
                index++;
            return index != values.length;
        }

        @Override
        public V next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturnedIndex = index++;
            return (V) values[lastReturnedIndex];
        }

        @Override
        public void remove() {
            checkLastReturnedIndex();

            if (values[lastReturnedIndex] != null) {
                values[lastReturnedIndex] = null;
                size--;
            }
            lastReturnedIndex = -1;
        }

        private void checkLastReturnedIndex() {
            if (lastReturnedIndex < 0)
                throw new IllegalStateException();
        }

    }


}
