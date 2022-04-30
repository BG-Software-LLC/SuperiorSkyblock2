package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * All the island events extend IslandEvent.
 */
public abstract class IslandEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    protected final Island island;

    /**
     * The constructor for the event.
     *
     * @param island The island object that was involved in the event.
     */
    public IslandEvent(Island island) {
        super(!Bukkit.isPrimaryThread());
        this.island = island;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the island that was involved in the event.
     */
    public Island getIsland() {
        return island;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
