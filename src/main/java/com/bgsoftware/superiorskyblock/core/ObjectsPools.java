package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.core.mutable.MutableObject;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ObjectsPools {

    public static final ObjectsPool<Wrapper<Location>> LOCATION = createNewPool(() -> new Location(null, 0D, 0D, 0D));
    public static final ObjectsPool<Wrapper<LazyWorldLocation>> LAZY_LOCATION = createNewPool(() -> new LazyWorldLocation(null, 0D, 0D, 0D));

    private ObjectsPools() {

    }

    public static <T> ObjectsPool<Wrapper<T>> createNewPool(ObjectsPool.Creator<T> creator) {
        MutableObject<ObjectsPool<Wrapper<T>>> wrapperReference = new MutableObject<>(null);

        ObjectsPool<Wrapper<T>> pool = new ObjectsPool<>(
                () -> new Wrapper<>(creator.create(),
                        obj -> onWrapperRelease(obj, wrapperReference.getValue())));

        wrapperReference.setValue(pool);

        return pool;
    }

    private static <T extends ObjectsPool.Releasable> void onWrapperRelease(T obj, @Nullable ObjectsPool<T> pool) {
        if (pool != null)
            pool.release(obj);
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
