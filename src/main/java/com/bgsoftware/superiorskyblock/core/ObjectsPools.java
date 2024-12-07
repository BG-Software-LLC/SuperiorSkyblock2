package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Location;

import java.util.function.Consumer;

public class ObjectsPools {

    public static final ObjectsPool<Wrapper<Location>> LOCATION = initializeLocationsPool();

    private static ObjectsPool<Wrapper<Location>> initializeLocationsPool() {
        return new ObjectsPool<>(() -> new Wrapper<>(new Location(null, 0D, 0D, 0D), LOCATION::release), 16);
    }

    private ObjectsPools() {

    }

    public static class Wrapper<T> implements ObjectsPool.Releasable, AutoCloseable {

        private final T handle;
        private final Consumer<Wrapper<T>> releaseMethod;

        Wrapper(T handle, Consumer<Wrapper<T>> releaseMethod) {
            this.handle = handle;
            this.releaseMethod = releaseMethod;
        }

        public T getHandle() {
            return handle;
        }

        @Override
        public void release() {
            this.releaseMethod.accept(this);
        }

        @Override
        public void close() {
            release();
        }
    }

}
