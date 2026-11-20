package com.bgsoftware.superiorskyblock.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

public class CachedResult<V, I> {

    private final AtomicLong threadOwner = new AtomicLong(-1);
    private long stamp = 0;

    private V cachedValue = null;
    private I identifier = null;

    public static <V, I> CachedResult<V, I> create() {
        return new CachedResult<>();
    }

    private CachedResult() {

    }

    public long startCache() {
        if (!threadOwner.compareAndSet(-1, Thread.currentThread().getId()))
            return -1;

        return ++stamp;
    }

    public void endCache(long stamp) {
        if (threadOwner.get() != Thread.currentThread().getId())
            return;

        if (stamp != this.stamp)
            return;

        this.cachedValue = null;
        this.identifier = null;
        this.threadOwner.set(-1);
    }

    public V getOrCompute(I identifier, Supplier<V> newValueSupplier) {
        boolean cacheEnabled = isEnabled();

        if (cacheEnabled && this.identifier == identifier)
            return this.cachedValue;

        V result = newValueSupplier.get();

        if (cacheEnabled) {
            update(result, identifier);
        }

        return result;
    }

    public V getOrCompute(I identifier, Function<I, V> newValueSupplier) {
        boolean cacheEnabled = isEnabled();

        if (cacheEnabled && this.identifier == identifier)
            return this.cachedValue;

        V result = newValueSupplier.apply(identifier);

        if (cacheEnabled) {
            update(result, identifier);
        }

        return result;
    }

    private boolean isEnabled() {
        return threadOwner.get() == Thread.currentThread().getId();
    }

    private void update(V value, I identifier) {
        this.cachedValue = value;
        this.identifier = identifier;
    }

}
