package com.bgsoftware.superiorskyblock.core.value;

import java.util.function.Supplier;

public class ValueSuppliedSynced<V extends Number> implements Value<V> {

    private final Supplier<V> supplier;

    ValueSuppliedSynced(Supplier<V> supplier) {
        this.supplier = supplier;
    }

    @Override
    public V get() {
        return supplier.get();
    }

    @Override
    public boolean isSynced() {
        return true;
    }
}
