package com.bgsoftware.superiorskyblock.core.events;

import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

public class CallbacksBus {

    private final EnumMap<CallbackType, List<Runnable>> CALLBACKS = new EnumMap<>(CallbackType.class);

    public CallbacksBus() {
    }

    public void registerCallback(CallbackType callbackType, Runnable callback) {
        CALLBACKS.computeIfAbsent(callbackType, c -> new LinkedList<>()).add(callback);
    }

    public void notifyCallbacks(CallbackType callbackType) {
        List<Runnable> callbacks = CALLBACKS.get(callbackType);
        if (callbacks != null)
            callbacks.forEach(callback -> {
                try {
                    callback.run();
                } catch (Throwable error) {
                    Log.error(error, "An unexpected error occurred while running callback:");
                }
            });
    }

    public enum CallbackType {

        SETTINGS_UPDATE

    }

}
