package com.bgsoftware.superiorskyblock.core.value;

import com.bgsoftware.common.annotations.Nullable;

import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

public interface IntValue {

    static IntValue fixed(int value) {
        return IntValueFixed.of(value);
    }

    static IntValue syncedFixed(int value) {
        return IntValueFixedSynced.of(value);
    }

    static IntValue syncedSupplied(IntSupplier supplier) {
        return IntValueSuppliedSynced.of(supplier);
    }

    static <K> Map<K, Integer> unboxMap(Map<K, IntValue> input) {
        return input.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get()
        ));
    }

    static int getNonSynced(@Nullable IntValue value, int syncedValue) {
        return value == null ? syncedValue : value.getNonSynced(syncedValue);
    }

    int get();

    boolean isSynced();

    default int getNonSynced(int syncedValue) {
        return isSynced() ? syncedValue : get();
    }

}
