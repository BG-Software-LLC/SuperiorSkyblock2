package com.bgsoftware.superiorskyblock.island.container.value;

public class FixedValue<V extends Number> implements Value<V> {

    private final V value;

    FixedValue(V value) {
        this.value = value;
    }

    @Override
    public V get() {
        return value;
    }

}
