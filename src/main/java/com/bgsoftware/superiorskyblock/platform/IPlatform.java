package com.bgsoftware.superiorskyblock.platform;

import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.args.IEventArgs;

public interface IPlatform {

    <Args extends IEventArgs> void notifyGameEvent(GameEvent<Args> gameEvent, GameEventPriority priority);

}
