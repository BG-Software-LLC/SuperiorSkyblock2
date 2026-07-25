package com.bgsoftware.superiorskyblock.core.value;

public class DoubleValueFixed implements DoubleValue {

    private static final ValuesCache<DoubleValueFixed> CACHE = new ValuesCache<>(DoubleValueFixed::new);

    private final double value;

    public static DoubleValueFixed of(double value) {
        return value == (int) value ? CACHE.fetch((int) value) : new DoubleValueFixed(value);
    }

    private DoubleValueFixed(double value) {
        this.value = value;
    }

    @Override
    public double get() {
        return this.value;
    }

    @Override
    public boolean isSynced() {
        return false;
    }
}
