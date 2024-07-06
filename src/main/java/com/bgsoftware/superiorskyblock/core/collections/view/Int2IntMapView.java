package com.bgsoftware.superiorskyblock.core.collections.view;

import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;

public interface Int2IntMapView {

    OptionalInt put(int key, int value);

    OptionalInt get(int key);

    default int getOrDefault(int key, int def) {
        OptionalInt value = get(key);
        return value.isPresent() ? value.getAsInt() : def;
    }

    OptionalInt remove(int key);

    int size();

    default boolean isEmpty() {
        return size() <= 0;
    }

    void clear();

    Iterator<Entry> entryIterator();

    IntIterator valueIterator();

    IntIterator keyIterator();

    Map<Integer, Integer> asMap();

    default int computeIfAbsent(int key, AbsentConsumer consumer) {
        OptionalInt valueOptional = get(key);

        int value;
        if (valueOptional.isPresent()) {
            value = valueOptional.getAsInt();
        } else {
            value = consumer.accept(key);
            put(key, value);
        }

        return value;
    }

    interface AbsentConsumer {

        int accept(int key);

    }

    interface Entry {

        int getKey();

        int getValue();

        int setValue(int newValue);

    }

}
