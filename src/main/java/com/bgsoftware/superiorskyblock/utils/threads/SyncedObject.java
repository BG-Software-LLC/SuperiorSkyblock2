package com.bgsoftware.superiorskyblock.utils.threads;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SyncedObject<T> {

    public static final SyncedObject EMPTY = SyncedObject.of(null);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private T value;

    SyncedObject(T value){
        this.value = value;
    }

    public T get(){
        if(value instanceof Map)
            throw new UnsupportedOperationException("Cannot get raw maps from synced objects.");

        if(value instanceof Collection)
            throw new UnsupportedOperationException("Cannot get raw collections from synced objects.");

        try{
            lock.readLock().lock();
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void set(T value){
        try{
            lock.writeLock().lock();
            this.value = value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void set(Function<T, T> function){
        try{
            lock.writeLock().lock();
            this.value = function.apply(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void read(Consumer<T> consumer){
        try{
            lock.readLock().lock();
            consumer.accept(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <R> R readAndGet(Function<T, R> function){
        try{
            lock.readLock().lock();
            return function.apply(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void write(Consumer<T> consumer){
        try{
            lock.writeLock().lock();
            consumer.accept(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <R> R writeAndGet(Function<T, R> function){
        try{
            lock.writeLock().lock();
            return function.apply(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static <T> SyncedObject<T> of(T value){
        return new SyncedObject<>(value);
    }

}
