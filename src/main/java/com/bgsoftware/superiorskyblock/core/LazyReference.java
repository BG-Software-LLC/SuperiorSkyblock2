package com.bgsoftware.superiorskyblock.core;

public abstract class LazyReference<E> {

    private E instance;

    public E get() {
        return instance == null ? (instance = create()) : instance;
    }

    protected abstract E create();

}
