package com.bgsoftware.superiorskyblock.core.mutable;

public class MutableObject<E> {

    private E value;

    public MutableObject(E value) {
        this.value = value;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }

}
