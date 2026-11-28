package com.bgsoftware.superiorskyblock.platform.event;

import com.bgsoftware.superiorskyblock.core.events.IEvent;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.platform.event.args.IEventArgs;

public class GameEvent<Args extends IEventArgs> implements IEvent<GameEventType<?>> {

    private final GameEventType<Args> type;
    private final Args args;

    private boolean cancelled = false;

    public GameEvent(GameEventType<Args> type, Args args) {
        this.type = type;
        this.args = args;
    }

    @Override
    public GameEventType<Args> getType() {
        return type;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    public Args getArgs() {
        return args;
    }

    public void setCancelled() {
        this.cancelled = true;
    }

    public boolean doesNotRecordBlockChange() {
        // Physics events are not listened to.
        if (this.type == GameEventType.BLOCK_PHYSICS_EVENT)
            return true;

        // A block_destroy generic game event (e.g. cactus self-breaking during its tick) is not
        // recorded by any handler, so it must not suppress the BlockUpdateShapeEvent path.
        if (this.type == GameEventType.GENERIC_GAME_EVENT)
            return "block_destroy".equals(((GameEventArgs.GenericGameEvent) this.args).gameEvent);

        return false;
    }

}
