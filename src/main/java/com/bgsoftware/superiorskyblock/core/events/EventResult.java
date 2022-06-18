package com.bgsoftware.superiorskyblock.core.events;

public class EventResult<T> {

    private final boolean cancelled;
    private final T result;

    private EventResult(boolean cancelled, T result) {
        this.cancelled = cancelled;
        this.result = result;
    }

    static <T> EventResult<T> of(boolean cancelled) {
        return new EventResult<>(cancelled, null);
    }

    static <T> EventResult<T> of(boolean cancelled, T result) {
        return new EventResult<>(cancelled, result);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public T getResult() {
        return result;
    }

}

