package com.bgsoftware.superiorskyblock.core.events;

import com.bgsoftware.superiorskyblock.commands.CommandsMap;
import com.bgsoftware.superiorskyblock.commands.player.CmdAdmin;
import com.bgsoftware.superiorskyblock.commands.player.CmdHelp;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.service.region.RegionManagerServiceImpl;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

public class CallbacksBus {

    private final EnumMap<CallbackType, List<Runnable>> CALLBACKS = new EnumMap<>(CallbackType.class);

    public CallbacksBus() {
    }

    public void registerDefaultCallbacks() {
        SIsland.registerCallbacks(this);
        CommandsMap.registerCallbacks(this);
        SpawnIsland.registerCallbacks(this);
        RegionManagerServiceImpl.registerCallbacks(this);
        CmdHelp.registerCallbacks(this);
        CmdAdmin.registerCallbacks(this);
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

        SETTINGS_UPDATE,
        COMMANDS_UPDATE

    }

}
