package com.bgsoftware.superiorskyblock.core.value;

public class IntValueFixed implements IntValue {

    private static final IntValueFixed[] CACHE = new IntValueFixed[64];

    private final int value;

    public static IntValueFixed of(int value) {
        if (value >= 0 && value < CACHE.length) {
            IntValueFixed intValueFixed = CACHE[value];
            if (intValueFixed == null)
                intValueFixed = CACHE[value] = new IntValueFixed(value);

            return intValueFixed;
        }

        return new IntValueFixed(value);
    }

    private IntValueFixed(int value) {
        this.value = value;
    }

    public int get() {
        return this.value;
    }

    @Override
    public boolean isSynced() {
        return false;
    }
}
