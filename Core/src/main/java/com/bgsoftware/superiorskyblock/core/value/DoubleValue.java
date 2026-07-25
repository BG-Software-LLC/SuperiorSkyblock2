package com.bgsoftware.superiorskyblock.core.value;

import com.bgsoftware.common.annotations.Nullable;

import java.util.function.DoubleSupplier;

public interface DoubleValue {

    static DoubleValue fixed(double value) {
        return DoubleValueFixed.of(value);
    }

    static DoubleValue syncedFixed(double value) {
        return DoubleValueFixedSynced.of(value);
    }

    static DoubleValue syncedSupplied(DoubleSupplier supplier) {
        return DoubleValueSuppliedSynced.of(supplier);
    }

    static double getNonSynced(@Nullable DoubleValue value, double syncedValue) {
        return value == null ? syncedValue : value.getNonSynced(syncedValue);
    }

    double get();

    boolean isSynced();

    default double getNonSynced(double syncedValue) {
        return isSynced() ? syncedValue : get();
    }

}
