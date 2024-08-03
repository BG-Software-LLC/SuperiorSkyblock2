package com.bgsoftware.superiorskyblock.core.mutable;

public class MutableBoolean {

    private boolean value;

    public MutableBoolean(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return this.value;
    }

    public void set(boolean value) {
        this.value = value;
    }

}
