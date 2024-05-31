package com.bgsoftware.superiorskyblock.core.value;

import java.util.function.DoubleSupplier;

public class DoubleValueSuppliedSynced implements DoubleValue {

    private final DoubleSupplier supplier;

    public static DoubleValueSuppliedSynced of(DoubleSupplier supplier) {
        return new DoubleValueSuppliedSynced(supplier);
    }

    private DoubleValueSuppliedSynced(DoubleSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public double get() {
        return supplier.getAsDouble();
    }

    @Override
    public boolean isSynced() {
        return true;
    }

}
