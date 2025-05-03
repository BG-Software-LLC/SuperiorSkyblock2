package com.bgsoftware.superiorskyblock.core.value;

import com.bgsoftware.common.annotations.Nullable;

import java.util.function.Supplier;

public interface Value<V> {

    static <V> Value<V> fixed(V value) {
        return new ValueFixed<>(value);
    }

    static <V> Value<V> syncedSupplied(Supplier<V> supplier) {
        return new ValueSuppliedSynced<>(supplier);
    }

    static <V> Value<V> syncedFixed(V value) {
        return new ValueFixedSynced<>(value);
    }

    static <V> V getNonSynced(@Nullable Value<V> value, V syncedValue) {
        return value == null ? syncedValue : value.getNonSynced(syncedValue);
    }

    V get();

    boolean isSynced();

    default V getNonSynced(V syncedValue) {
        return isSynced() ? syncedValue : get();
    }

}
