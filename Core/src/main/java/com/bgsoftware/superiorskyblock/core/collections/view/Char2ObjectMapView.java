package com.bgsoftware.superiorskyblock.core.collections.view;

import com.bgsoftware.common.annotations.Nullable;

import java.util.Iterator;

public interface Char2ObjectMapView<V> {

    @Nullable
    V put(char key, V value);

    @Nullable
    V get(char key);

    default V getOrDefault(char key, V def) {
        V value = get(key);
        return value == null ? def : value;
    }

    @Nullable
    V remove(char key);

    int size();

    default boolean isEmpty() {
        return size() <= 0;
    }

    void clear();

    Iterator<Entry<V>> entryIterator();

    Iterator<V> valueIterator();

    CharIterator keyIterator();

    default V computeIfAbsent(char key, AbsentConsumer<V> consumer) {
        V value = get(key);
        if (value == null) {
            value = consumer.accept(key);
            put(key, value);
        }

        return value;
    }

    interface AbsentConsumer<V> {

        V accept(char key);

    }

    interface Entry<V> {

        char getKey();

        V getValue();

        V setValue(V newValue);

    }

}
