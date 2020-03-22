package com.bgsoftware.superiorskyblock.utils.threads;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SyncedObject<T> {

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private T value;

    private SyncedObject(T value){
        this.value = value;
    }

    public T get(){
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

    public <R> R run(Function<T, R> function){
        try{
            lock.readLock().lock();
            return function.apply(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void run(Consumer<T> consumer){
        try{
            lock.writeLock().lock();
            consumer.accept(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static <T> SyncedObject<T> of(T value){
        return new SyncedObject<>(value);
    }

}
