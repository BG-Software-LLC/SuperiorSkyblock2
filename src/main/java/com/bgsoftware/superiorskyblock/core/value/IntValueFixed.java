package com.bgsoftware.superiorskyblock.core.value;

public class IntValueFixed implements IntValue {

    private static final ValuesCache<IntValueFixed> CACHE = new ValuesCache<>(IntValueFixed::new);

    private final int value;

    public static IntValueFixed of(int value) {
        return CACHE.fetch(value);
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
