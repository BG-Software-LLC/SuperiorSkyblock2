package com.bgsoftware.superiorskyblock.platform.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.events.EventsDispatcher;
import com.bgsoftware.superiorskyblock.listener.AbstractGameEventListener;

public class GameEventsDispatcher extends EventsDispatcher<
        AbstractGameEventListener,
        GameEventType<?>,
        GameEventPriority,
        GameEvent<?>> {

    public GameEventsDispatcher(SuperiorSkyblockPlugin plugin) {
        super(plugin, GameEventPriority.class, GameEventType.values());
    }

}
