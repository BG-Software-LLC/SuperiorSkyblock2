package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.platform.event.GameEventCallback;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.IEventArgs;

public abstract class AbstractGameEventListener {


    protected final SuperiorSkyblockPlugin plugin;

    protected AbstractGameEventListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    protected <Args extends IEventArgs> void registerCallback(GameEventType<Args> type, GameEventPriority priority,
                                                              GameEventCallback<Args> callback) {
        registerCallback(type, priority, true, callback);
    }

    protected <Args extends IEventArgs> void registerCallback(GameEventType<Args> type, GameEventPriority priority,
                                                              boolean ignoreCancelled,
                                                              GameEventCallback<Args> callback) {
        plugin.getGameEventsDispatcher().registerCallback(this, type, priority, ignoreCancelled, callback);
    }


}
