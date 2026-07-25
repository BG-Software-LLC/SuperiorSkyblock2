package com.bgsoftware.superiorskyblock.core;

import java.util.LinkedList;
import java.util.Queue;

public class ObjectsPool<T extends ObjectsPool.Releasable> {

    private final Queue<T> backend = new LinkedList<>();
    private final Creator<T> creator;

    public ObjectsPool(Creator<T> creator) {
        this(creator, 0);
    }

    public ObjectsPool(Creator<T> creator, int initialCapacity) {
        this.creator = creator;
        for (int i = 0; i < initialCapacity; ++i)
            this.backend.offer(creator.create());
    }

    public T obtain() {
        T obj;
        synchronized (this) {
            obj = this.backend.poll();
        }
        return obj == null ? this.creator.create() : obj;
    }

    public void release(T obj) {
        synchronized (this) {
            this.backend.offer(obj);
        }
    }

    public interface Creator<T> {

        T create();

    }

    public interface Releasable {

        void release();

    }

}
