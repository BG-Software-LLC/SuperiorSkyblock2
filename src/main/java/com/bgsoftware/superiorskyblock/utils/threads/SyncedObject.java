package com.bgsoftware.superiorskyblock.utils.threads;

import java.util.function.Consumer;
import java.util.function.Function;

public final class SyncedObject<T> {

    private T value;

    private SyncedObject(T value){
        this.value = value;
    }

    public T get(){
        synchronized (this){
            return value;
        }
    }

    public void set(T value){
        synchronized (this){
            this.value = value;
        }
    }

    public <R> R run(Function<T, R> function){
        synchronized (this){
            return function.apply(value);
        }
    }

    public void run(Consumer<T> consumer){
        synchronized (this){
            consumer.accept(value);
        }
    }

    public static <T> SyncedObject<T> of(T value){
        return new SyncedObject<>(value);
    }

}
