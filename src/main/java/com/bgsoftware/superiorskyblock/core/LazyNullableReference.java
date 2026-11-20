package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

import java.util.Optional;

public abstract class LazyNullableReference<E> {

    private boolean computed = false;
    @Nullable
    private E instance;

    @Nullable
    public E get() {
        if (!this.computed) {
            this.instance = create();
            this.computed = true;
        }

        return this.instance;
    }

    @Nullable
    protected abstract E create();

    public Optional<E> getIfPresent() {
        return Optional.ofNullable(this.instance);
    }

    @Nullable
    public E getIfPresentOrNull() {
        return this.instance;
    }

    public void reset() {
        this.computed = false;
        this.instance = null;
    }

}
