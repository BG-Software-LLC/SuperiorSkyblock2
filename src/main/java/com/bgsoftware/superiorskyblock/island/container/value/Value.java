package com.bgsoftware.superiorskyblock.island.container.value;

import java.util.function.Supplier;

public interface Value<V extends Number> {

    V get();

    static <V extends Number> Value<V> fixed(V value) {
        return new FixedValue<>(value);
    }

    static <V extends Number> Value<V> syncedSupplied(Supplier<V> supplier) {
        return new SuppliedSyncedValue<>(supplier);
    }

    static <V extends Number> Value<V> syncedFixed(V value) {
        return new FixedSyncedValue<>(value);
    }

}
