package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PluginInitializeEvent is called when other plugins needs to register their custom data.
 */
public class PluginInitializeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorSkyblock plugin;

    /**
     * The constructor for the event.
     * You cannot use handlers in this time, as none of them is set up.
     */
    public PluginInitializeEvent(SuperiorSkyblock plugin) {
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
