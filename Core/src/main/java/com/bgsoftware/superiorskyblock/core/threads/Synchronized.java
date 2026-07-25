package com.bgsoftware.superiorskyblock.core.threads;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class Synchronized<T> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private T value;

    Synchronized(T value) {
        this.value = value;
    }

    public static <T> Synchronized<T> of(T value) {
        return new Synchronized<>(value);
    }

    public T get() {
        if (value instanceof Map)
            throw new UnsupportedOperationException("Cannot get raw maps from synced objects.");

        if (value instanceof Collection)
            throw new UnsupportedOperationException("Cannot get raw collections from synced objects.");

        try {
            lock.readLock().lock();
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    public T set(T value) {
        try {
            lock.writeLock().lock();
            T oldValue = this.value;
            this.value = value;
            return oldValue;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void set(Function<T, T> function) {
        try {
            lock.writeLock().lock();
            this.value = function.apply(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void read(Consumer<T> consumer) {
        try {
            lock.readLock().lock();
            consumer.accept(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <R> R readAndGet(Function<T, R> function) {
        try {
            lock.readLock().lock();
            return function.apply(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void write(Consumer<T> consumer) {
        try {
            lock.writeLock().lock();
            consumer.accept(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <R> R writeAndGet(Function<T, R> function) {
        try {
            lock.writeLock().lock();
            return function.apply(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

}
