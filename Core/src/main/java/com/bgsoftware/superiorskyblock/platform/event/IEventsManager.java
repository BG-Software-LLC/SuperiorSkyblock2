package com.bgsoftware.superiorskyblock.platform.event;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.platform.event.args.IEventArgs;
import org.bukkit.event.Event;

public interface IEventsManager {

    void registerGameEventsListener();

    void unregisterGameEventsListener();

    interface GameEventCreator<Args extends IEventArgs, E extends Event> {

        @Nullable
        GameEvent<Args> execute(GameEventType<Args> eventType, GameEventPriority priority, E e);

    }

}
