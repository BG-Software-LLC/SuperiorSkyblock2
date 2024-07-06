package com.bgsoftware.superiorskyblock.core.value;

public class DoubleValueFixed implements DoubleValue {

    private static final DoubleValueFixed[] CACHE = new DoubleValueFixed[64];

    private final double value;

    public static DoubleValueFixed of(double value) {
        if (value >= 0 && value < CACHE.length && value == (int) value) {
            DoubleValueFixed intValueFixed = CACHE[(int) value];
            if (intValueFixed == null)
                intValueFixed = CACHE[(int) value] = new DoubleValueFixed(value);

            return intValueFixed;
        }

        return new DoubleValueFixed(value);
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
