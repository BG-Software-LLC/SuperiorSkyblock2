package com.bgsoftware.superiorskyblock.api.platform;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

/**
 * The events dispatcher is used to dispatch events to the plugin.
 */
public interface IEventsDispatcher {

    /**
     * Notify about a new game-event.
     *
     * @param event    The event that was fired.
     * @param priority The priority of the event.
     * @return Whether the event was successfully dispatched.
     */
    boolean notifyEvent(Event event, EventPriority priority);

    /**
     * Whether the default executor should be used if {@link #notifyEvent(Event, EventPriority)} fails.
     */
    default boolean shouldFallbackToDefaultExecutorOnFailure() {
        return true;
    }

}
