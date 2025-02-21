package com.bgsoftware.superiorskyblock.core.events;

public interface IEvent<T> {

    T getType();

    boolean isCancelled();

}
