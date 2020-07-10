package com.bgsoftware.superiorskyblock.utils.threads;

public final class MutableObject<T> {

    private T object;

    private MutableObject(T object){
        this.object = object;
    }

    public T get(){
        return object;
    }

    public void set(T object){
        this.object = object;
    }

    public static <T> MutableObject<T> of(T object){
        return new MutableObject<>(object);
    }

}
