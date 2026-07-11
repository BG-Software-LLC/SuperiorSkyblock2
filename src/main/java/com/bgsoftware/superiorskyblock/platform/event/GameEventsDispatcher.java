package com.bgsoftware.superiorskyblock.platform.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.events.EventsDispatcher;
import com.bgsoftware.superiorskyblock.listener.AbstractGameEventListener;

public class GameEventsDispatcher extends EventsDispatcher<
        AbstractGameEventListener,
        GameEventType<?>,
        GameEventPriority,
        GameEvent<?>> {

    @GameEventFlags
    private int capturedEventsFlags = 0;

    public GameEventsDispatcher(SuperiorSkyblockPlugin plugin) {
        super(plugin, GameEventPriority.class, GameEventType.values());
    }

    @Override
    public void startCaptureEvents() {
        this.startCaptureEvents(0xFFFFFFFF);
    }

    public void startCaptureEvents(@GameEventFlags int capturedEventsFlags) {
        super.startCaptureEvents();
        this.capturedEventsFlags = capturedEventsFlags;
    }

    @Override
    protected boolean filterCapturedEvent(GameEvent<?> event) {
        return this.capturedEventsFlags == 0xFFFFFFFF || (event.getType().getFlags() & this.capturedEventsFlags) != 0;
    }

}
