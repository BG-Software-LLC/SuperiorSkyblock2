package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PluginLoadDataEvent is called right before the plugin starts to load data.
 */
public class PluginLoadDataEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorSkyblock plugin;

    private boolean cancelled = false;

    /**
     * The constructor for the event.
     *
     * @param plugin The instance of the plugin.
     */
    public PluginLoadDataEvent(SuperiorSkyblock plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the instance of the plugin.
     */
    public SuperiorSkyblock getPlugin() {
        return plugin;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
