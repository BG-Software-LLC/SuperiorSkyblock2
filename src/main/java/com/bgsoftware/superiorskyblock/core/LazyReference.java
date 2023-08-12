package com.bgsoftware.superiorskyblock.core;

import java.util.Optional;

public abstract class LazyReference<E> {

    private E instance;

    public E get() {
        return instance == null ? (instance = create()) : instance;
    }

    protected abstract E create();

    public Optional<E> getIfPresent() {
        return Optional.ofNullable(this.instance);
    }

}
