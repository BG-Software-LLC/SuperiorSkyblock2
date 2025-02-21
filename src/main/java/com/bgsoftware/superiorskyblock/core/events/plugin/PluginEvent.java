package com.bgsoftware.superiorskyblock.core.events.plugin;

import com.bgsoftware.superiorskyblock.core.events.IEvent;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;

public class PluginEvent<Args extends PluginEventArgs> implements IEvent<PluginEventType<?>> {

    private final PluginEventType<Args> type;
    private final Args args;

    private boolean cancelled = false;

    public PluginEvent(PluginEventType<Args> type, Args args) {
        this.type = type;
        this.args = args;
    }

    @Override
    public PluginEventType<Args> getType() {
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

}
