package com.bgsoftware.superiorskyblock.tag;

public abstract class NumberTag<E extends Number> extends Tag<E> {

    public NumberTag(E value, Class<?> clazz, Class<?>... parameterTypes) {
        super(value, clazz, parameterTypes);
    }

}