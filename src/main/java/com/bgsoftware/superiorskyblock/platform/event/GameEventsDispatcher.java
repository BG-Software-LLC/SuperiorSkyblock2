package com.bgsoftware.superiorskyblock.platform.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.events.EventsDispatcher;
import com.bgsoftware.superiorskyblock.listener.AbstractGameEventListener;

import java.util.List;

public class GameEventsDispatcher extends EventsDispatcher<
        AbstractGameEventListener,
        GameEventType<?>,
        GameEventPriority,
        GameEvent<?>> {

    @GameEventFlags
    private final ThreadLocal<Integer> capturedEventsFlags = ThreadLocal.withInitial(() -> 0);

    public GameEventsDispatcher(SuperiorSkyblockPlugin plugin) {
        super(plugin, GameEventPriority.class, GameEventType.values());
    }

    @Override
    public void startCaptureEvents() {
        this.startCaptureEvents(0xFFFFFFFF);
    }

    public void startCaptureEvents(@GameEventFlags int capturedEventsFlags) {
        super.startCaptureEvents();
        this.capturedEventsFlags.set(capturedEventsFlags);
    }

    @Override
    public List<GameEvent<?>> stopCaptureEvents() {
        capturedEventsFlags.remove();
        return super.stopCaptureEvents();
    }

    @Override
    protected boolean filterCapturedEvent(GameEvent<?> event) {
        int flags = this.capturedEventsFlags.get();
        return flags == 0xFFFFFFFF || (event.getType().getFlags() & flags) != 0;
    }

}
