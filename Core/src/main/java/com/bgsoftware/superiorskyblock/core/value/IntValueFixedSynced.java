package com.bgsoftware.superiorskyblock.core.value;

public class IntValueFixedSynced implements IntValue {

    private static final ValuesCache<IntValueFixedSynced> CACHE = new ValuesCache<>(IntValueFixedSynced::new);

    private final int value;

    public static IntValueFixedSynced of(int value) {
        return CACHE.fetch(value);
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
