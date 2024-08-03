package com.bgsoftware.superiorskyblock.core.collections.view;

import com.bgsoftware.common.annotations.Nullable;

import java.util.Iterator;

public interface Long2ObjectMapView<V> {

    @Nullable
    V put(long key, V value);

    @Nullable
    V get(long key);

    default V getOrDefault(long key, V def) {
        V value = get(key);
        return value == null ? def : value;
    }

    @Nullable
    V remove(long key);

    int size();

    default boolean isEmpty() {
        return size() <= 0;
    }

    void clear();

    Iterator<Entry<V>> entryIterator();

    Iterator<V> valueIterator();

    LongIterator keyIterator();

    default V computeIfAbsent(long key, AbsentConsumer<V> consumer) {
        V value = get(key);
        if (value == null) {
            value = consumer.accept(key);
            put(key, value);
        }

        return value;
    }

    interface AbsentConsumer<V> {

        V accept(long key);

    }

    interface Entry<V> {

        long getKey();

        V getValue();

        V setValue(V newValue);

    }

}
