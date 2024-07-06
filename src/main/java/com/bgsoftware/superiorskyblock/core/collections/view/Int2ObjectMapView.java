package com.bgsoftware.superiorskyblock.core.collections.view;

import com.bgsoftware.common.annotations.Nullable;

import java.util.Iterator;

public interface Int2ObjectMapView<V> {

    @Nullable
    V put(int key, V value);

    @Nullable
    V get(int key);

    default V getOrDefault(int key, V def) {
        V value = get(key);
        return value == null ? def : value;
    }

    @Nullable
    V remove(int key);

    int size();

    default boolean isEmpty() {
        return size() <= 0;
    }

    void clear();

    Iterator<Entry<V>> entryIterator();

    Iterator<V> valueIterator();

    IntIterator keyIterator();

    default V computeIfAbsent(int key, AbsentConsumer<V> consumer) {
        V value = get(key);
        if (value == null) {
            value = consumer.accept(key);
            put(key, value);
        }

        return value;
    }

    interface AbsentConsumer<V> {

        V accept(int key);

    }

    interface Entry<V> {

        int getKey();

        V getValue();

        V setValue(V newValue);

    }


}
