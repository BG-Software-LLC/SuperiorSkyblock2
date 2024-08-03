package com.bgsoftware.superiorskyblock.core.value;

import java.util.function.IntSupplier;

public class IntValueSuppliedSynced implements IntValue {

    private final IntSupplier supplier;

    public static IntValueSuppliedSynced of(IntSupplier supplier) {
        return new IntValueSuppliedSynced(supplier);
    }

    private IntValueSuppliedSynced(IntSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public int get() {
        return supplier.getAsInt();
    }

    @Override
    public boolean isSynced() {
        return true;
    }

}
