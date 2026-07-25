package com.bgsoftware.superiorskyblock.platform.event;

import com.bgsoftware.superiorskyblock.core.events.EventCallback;
import com.bgsoftware.superiorskyblock.platform.event.args.IEventArgs;

public interface GameEventCallback<Args extends IEventArgs> extends EventCallback<GameEvent<Args>> {

    void execute(GameEvent<Args> gameEvent);

}
