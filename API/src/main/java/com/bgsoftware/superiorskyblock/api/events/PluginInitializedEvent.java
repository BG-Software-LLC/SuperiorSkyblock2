package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PluginInitializedEvent is called when plugin enabled completely.
 */
public class PluginInitializedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorSkyblock plugin;

    /**
     * The constructor for the event.
     */
    public PluginInitializedEvent(SuperiorSkyblock plugin) {
        this.plugin = plugin;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public SuperiorSkyblock getPlugin() {
        return plugin;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
