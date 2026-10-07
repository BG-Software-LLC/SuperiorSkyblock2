package com.bgsoftware.superiorskyblock.core.events;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventsDispatcher<L, T extends EventType<?, ?>, P extends Enum<P>, E extends IEvent<T>> {

    protected EnumerateMap<T, EnumMap<P, List<RegisteredListener>>> callbacks;
    private final Object callbacksLock = new Object();
    private final Set<T> registeredTypes = Collections.newSetFromMap(new IdentityHashMap<>());
    private final ThreadLocal<EnumerateMap<T, EnumMap<P, List<RegisteredListener>>>> pendingCallbacks = new ThreadLocal<>();

    protected final SuperiorSkyblockPlugin plugin;
    protected final Class<P> priorityClass;

    // This must be thread-safe, as it can be accessed from multiple threads.
    // We don't actually want it to be thread-safe, but per-thread captured events.
    // If we don't do that, weird behaviors can occur. For reference:
    // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/2863
    private final ThreadLocal<List<E>> capturedEvents = new ThreadLocal<>();

    protected EventsDispatcher(SuperiorSkyblockPlugin plugin, Class<P> priorityClass, Collection<T> allTypes) {
        this.plugin = plugin;
        this.priorityClass = priorityClass;
        this.callbacks = new EnumerateMap<>(allTypes);
        this.registeredTypes.addAll(allTypes);
    }

    public void registerCallback(L listener, T type, P priority, boolean ignoreCancelled, EventCallback callback) {
        synchronized (callbacksLock) {
            this.registeredTypes.add(type);
            EnumerateMap<T, EnumMap<P, List<RegisteredListener>>> callbacks = this.pendingCallbacks.get();
            if (callbacks == null)
                callbacks = this.callbacks;
            EnumMap<P, List<RegisteredListener>> updated = new EnumMap<>(this.priorityClass);
            EnumMap<P, List<RegisteredListener>> previous = callbacks.get(type);
            if (previous != null)
                updated.putAll(previous);
            List<RegisteredListener> listeners = new ArrayList<>(updated.getOrDefault(priority, Collections.emptyList()));
            listeners.add(new RegisteredListener(listener, ignoreCancelled, callback));
            updated.put(priority, Collections.unmodifiableList(listeners));
            callbacks.put(type, updated);
        }
    }

    public void replaceCallbacks(Collection<L> previousListeners, Runnable registration) {
        if (this.pendingCallbacks.get() != null)
            throw new IllegalStateException("Callback registration is already in progress");
        EnumerateMap<T, EnumMap<P, List<RegisteredListener>>> replacement;
        synchronized (callbacksLock) {
            replacement = new EnumerateMap<>(this.callbacks);
        }
        replacement.clear();
        this.pendingCallbacks.set(replacement);
        try {
            registration.run();
        } finally {
            this.pendingCallbacks.remove();
        }
        Set<L> removedListeners = Collections.newSetFromMap(new IdentityHashMap<>());
        removedListeners.addAll(previousListeners);
        synchronized (callbacksLock) {
            EnumerateMap<T, EnumMap<P, List<RegisteredListener>>> updatedCallbacks = new EnumerateMap<>(this.callbacks);
            for (T type : this.registeredTypes) {
                EnumMap<P, List<RegisteredListener>> updated = new EnumMap<>(this.priorityClass);
                EnumMap<P, List<RegisteredListener>> previous = this.callbacks.get(type);
                if (previous != null) {
                    previous.forEach((priority, listeners) -> {
                        List<RegisteredListener> retained = new ArrayList<>();
                        for (RegisteredListener listener : listeners) {
                            if (!removedListeners.contains(listener.listener))
                                retained.add(listener);
                        }
                        if (!retained.isEmpty())
                            updated.put(priority, retained);
                    });
                }
                EnumMap<P, List<RegisteredListener>> added = replacement.get(type);
                if (added != null)
                    added.forEach((priority, listeners) -> updated.computeIfAbsent(priority, ignored -> new ArrayList<>()).addAll(listeners));
                updated.replaceAll((priority, listeners) -> Collections.unmodifiableList(listeners));
                if (updated.isEmpty()) {
                    if (previous != null)
                        updatedCallbacks.remove(type);
                } else {
                    updatedCallbacks.put(type, updated);
                }
            }
            this.callbacks = updatedCallbacks;
        }
    }

    public boolean hasCallbacks(T eventType, P priority) {
        Map<P, List<RegisteredListener>> registered = getRegisteredCallbacks(eventType);
        return registered != null && registered.containsKey(priority);
    }

    public void clearCallbacks() {
        synchronized (callbacksLock) {
            this.callbacks.clear();
        }
    }

    public void startCaptureEvents() {
        this.capturedEvents.set(new LinkedList<>());
    }

    public List<E> stopCaptureEvents() {
        List<E> capturedEvents = this.capturedEvents.get();
        this.capturedEvents.remove();
        return capturedEvents;
    }

    public void onGameEvent(E event, P priority) {
        if (event.isCancelled())
            return;

        Map<P, List<RegisteredListener>> gameEventCallbacks = getRegisteredCallbacks(event.getType());
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

        List<E> capturedEvents = this.capturedEvents.get();
        if (capturedEvents != null && filterCapturedEvent(event))
            capturedEvents.add(event);
    }

    protected boolean filterCapturedEvent(E event) {
        return true;
    }

    public Map<P, List<EventCallback>> getCallbacks(T eventType) {
        Map<P, List<RegisteredListener>> priorityListeners = getRegisteredCallbacks(eventType);

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

    protected Map<P, List<RegisteredListener>> getRegisteredCallbacks(T eventType) {
        synchronized (callbacksLock) {
            return callbacks.get(eventType);
        }
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
