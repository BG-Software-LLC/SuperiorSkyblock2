package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

import java.util.Optional;

public abstract class LazyNullableReference<E, A> {

    private boolean computed = false;
    @Nullable
    private E instance;

    @Nullable
    public E get() {
        return get(null);
    }

    /* Called when the ::get method is used in a lambda with a parameter */
    public E get(@Nullable A arg) {
        if (!this.computed) {
            this.instance = create(arg);
            this.computed = true;
        }

        return this.instance;
    }

    @Nullable
    protected abstract E create(A arg);

    public Optional<E> getIfPresent() {
        return Optional.ofNullable(this.instance);
    }

    @Nullable
    public E getIfPresentOrNull() {
        return this.instance;
    }

    /* Takes the value that another reference already computed, unless this one computed its own. */
    public void copyComputedFrom(LazyNullableReference<E, ?> other) {
        if (!this.computed && other.computed) {
            this.instance = other.instance;
            this.computed = true;
        }
    }

    public void reset() {
        this.computed = false;
        this.instance = null;
    }

}
