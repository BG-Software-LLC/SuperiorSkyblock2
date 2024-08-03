package com.bgsoftware.superiorskyblock.core.value;

public class ValueFixedSynced<V extends Number> implements Value<V> {

    private final V value;

    ValueFixedSynced(V value) {
        this.value = value;
    }

    @Override
    public V get() {
        return value;
    }

    @Override
    public boolean isSynced() {
        return true;
    }
}
