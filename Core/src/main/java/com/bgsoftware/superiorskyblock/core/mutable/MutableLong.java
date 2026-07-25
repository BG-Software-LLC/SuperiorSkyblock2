package com.bgsoftware.superiorskyblock.core.mutable;

public class MutableLong {

    private long value;

    public MutableLong(long value) {
        this.value = value;
    }

    public long get() {
        return this.value;
    }

    public void set(long value) {
        this.value = value;
    }

}
