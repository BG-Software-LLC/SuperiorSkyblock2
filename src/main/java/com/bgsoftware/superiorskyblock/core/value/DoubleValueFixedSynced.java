package com.bgsoftware.superiorskyblock.core.value;

public class DoubleValueFixedSynced implements DoubleValue {

    private static final DoubleValueFixedSynced[] CACHE = new DoubleValueFixedSynced[64];

    private final double value;

    public static DoubleValueFixedSynced of(double value) {
        if (value >= 0 && value < CACHE.length && value == (int) value) {
            DoubleValueFixedSynced intValueFixedSynced = CACHE[(int) value];
            if (intValueFixedSynced == null)
                intValueFixedSynced = CACHE[(int) value] = new DoubleValueFixedSynced(value);

            return intValueFixedSynced;
        }

        return new DoubleValueFixedSynced(value);
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
