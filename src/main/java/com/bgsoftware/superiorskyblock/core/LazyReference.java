package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

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

    @Nullable
    public E getIfPresentOrNull() {
        return this.instance;
    }

    public void reset() {
        this.instance = null;
    }

}
