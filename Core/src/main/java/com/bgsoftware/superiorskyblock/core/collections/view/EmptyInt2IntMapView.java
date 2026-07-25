package com.bgsoftware.superiorskyblock.core.collections.view;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;

public class EmptyInt2IntMapView implements Int2IntMapView {

    public static final EmptyInt2IntMapView INSTANCE = new EmptyInt2IntMapView();

    private EmptyInt2IntMapView() {

    }

    @Override
    public OptionalInt put(int key, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OptionalInt get(int key) {
        return OptionalInt.empty();
    }

    @Override
    public OptionalInt remove(int key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Entry> entryIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public IntIterator valueIterator() {
        return EmptyIntIterator.INSTANCE;
    }

    @Override
    public IntIterator keyIterator() {
        return EmptyIntIterator.INSTANCE;
    }

    @Override
    public Map<Integer, Integer> asMap() {
        return Collections.emptyMap();
    }

}
