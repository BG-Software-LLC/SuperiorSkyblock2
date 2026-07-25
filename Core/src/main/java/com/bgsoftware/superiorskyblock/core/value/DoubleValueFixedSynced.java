package com.bgsoftware.superiorskyblock.core.value;

public class DoubleValueFixedSynced implements DoubleValue {

    private static final ValuesCache<DoubleValueFixedSynced> CACHE = new ValuesCache<>(DoubleValueFixedSynced::new);

    private final double value;

    public static DoubleValueFixedSynced of(double value) {
        return value == (int) value ? CACHE.fetch((int) value) : new DoubleValueFixedSynced(value);
    }

    private DoubleValueFixedSynced(double value) {
        this.value = value;
    }

    @Override
    public double get() {
        return value;
    }

    @Override
    public boolean isSynced() {
        return true;
    }
}
