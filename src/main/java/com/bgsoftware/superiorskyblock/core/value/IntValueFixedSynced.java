package com.bgsoftware.superiorskyblock.core.value;

public class IntValueFixedSynced implements IntValue {

    private static final IntValueFixedSynced[] CACHE = new IntValueFixedSynced[64];

    private final int value;

    public static IntValueFixedSynced of(int value) {
        if (value >= 0 && value < CACHE.length) {
            IntValueFixedSynced intValueFixedSynced = CACHE[value];
            if (intValueFixedSynced == null)
                intValueFixedSynced = CACHE[value] = new IntValueFixedSynced(value);

            return intValueFixedSynced;
        }

        return new IntValueFixedSynced(value);
    }

    private IntValueFixedSynced(int value) {
        this.value = value;
    }

    @Override
    public int get() {
        return value;
    }

    @Override
    public boolean isSynced() {
        return true;
    }
}
