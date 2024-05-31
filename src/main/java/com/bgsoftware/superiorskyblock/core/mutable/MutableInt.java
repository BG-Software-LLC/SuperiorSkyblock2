package com.bgsoftware.superiorskyblock.core.mutable;

public class MutableInt {

    private int value;

    public MutableInt(int value) {
        this.value = value;
    }

    public int get() {
        return this.value;
    }

    public void set(int value) {
        this.value = value;
    }

}
