package com.bgsoftware.superiorskyblock.core;

public abstract class Singleton<E> {

    private E instance;

    public E get() {
        return instance == null ? (instance = create()) : instance;
    }

    protected abstract E create();

}
