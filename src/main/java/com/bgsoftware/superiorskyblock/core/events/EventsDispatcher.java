package com.bgsoftware.superiorskyblock.core.events;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventsDispatcher<L, T extends EventType<?, ?>, P extends Enum<P>, E extends IEvent<T>> {

    protected final EnumerateMap<T, EnumMap<P, List<RegisteredListener>>> callbacks;

    protected final SuperiorSkyblockPlugin plugin;
    protected final Class<P> priorityClass;

    protected EventsDispatcher(SuperiorSkyblockPlugin plugin, Class<P> priorityClass, Collection<T> allTypes) {
        this.plugin = plugin;
        this.priorityClass = priorityClass;
        this.callbacks = new EnumerateMap<>(allTypes);
    }

    public void registerCallback(L listener, T type, P priority, boolean ignoreCancelled, EventCallback callback) {
        callbacks.computeIfAbsent(type, t -> new EnumMap<>(this.priorityClass))
                .computeIfAbsent(priority, p -> new LinkedList<>())
                .add(new RegisteredListener(listener, ignoreCancelled, callback));
    }

    public void clearCallbacks() {
        this.callbacks.clear();
    }

    public void onGameEvent(E event, P priority) {
        if (event.isCancelled())
            return;

        EnumMap<P, List<RegisteredListener>> gameEventCallbacks = callbacks.get(event.getType());
        if (gameEventCallbacks != null) {
            List<RegisteredListener> priorityCallbacks = gameEventCallbacks.get(priority);
            if (priorityCallbacks != null) {
                for (RegisteredListener listener : priorityCallbacks) {
                    if (listener.ignoreCancelled && event.isCancelled()) {
                        continue;
                    }

                    try {
                        listener.callback.execute(event);
                    } catch (Throwable error) {
                        Log.error(error, "Could not pass listener: " + listener.listener);
                    }
                }
            }
        }
    }

    public Map<P, List<EventCallback>> getCallbacks(T eventType) {
        Map<P, List<RegisteredListener>> priorityListeners = callbacks.get(eventType);

        if (priorityListeners == null || priorityListeners.isEmpty())
            return Collections.emptyMap();

        Map<P, List<EventCallback>> priorityCallbacks = new EnumMap<>(this.priorityClass);

        priorityListeners.forEach((eventPriority, listeners) -> {
            List<EventCallback> callbacks = new LinkedList<>();
            listeners.forEach(listener -> callbacks.add(listener.callback));
            priorityCallbacks.put(eventPriority, callbacks);
        });

        return priorityCallbacks;
    }

    protected class RegisteredListener {

        public final L listener;
        public final boolean ignoreCancelled;
        public final EventCallback callback;

        RegisteredListener(L listener, boolean ignoreCancelled, EventCallback callback) {
            this.listener = listener;
            this.ignoreCancelled = ignoreCancelled;
            this.callback = callback;
        }

    }

}
