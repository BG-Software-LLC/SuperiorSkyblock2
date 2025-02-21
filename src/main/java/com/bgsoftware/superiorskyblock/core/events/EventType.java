package com.bgsoftware.superiorskyblock.core.events;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;

public abstract class EventType<Args, E> implements Enumerable {

    private static int ordinalCounter = 0;

    private final int ordinal;

    protected EventType() {
        this.ordinal = ordinalCounter++;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    public abstract E createEvent(Args args);

}
