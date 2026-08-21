package com.bgsoftware.superiorskyblock.core;

import java.util.OptionalInt;

public abstract class LazyInt {

    private volatile int value;
    private volatile boolean assigned = false;

    public synchronized int get() {
        if (!this.assigned) {
            this.value = create();
            this.assigned = true;
        }

        return this.value;
    }

    protected abstract int create();

    public OptionalInt getIfPresent() {
        return this.assigned ? OptionalInt.of(this.value) : OptionalInt.empty();
    }

}
