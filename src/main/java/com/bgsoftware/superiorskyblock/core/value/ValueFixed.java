package com.bgsoftware.superiorskyblock.core.value;

public class ValueFixed<V extends Number> implements Value<V> {

    private final V value;

    ValueFixed(V value) {
        this.value = value;
    }

    @Override
    public V get() {
        return value;
    }

    @Override
    public boolean isSynced() {
        return false;
    }
}
