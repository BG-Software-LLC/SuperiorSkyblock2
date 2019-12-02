package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
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
     * @param island The island object that was involved in the event.
     */
    public IslandEvent(Island island){
        this.island = island;
    }

    /**
     * Get the island object that was involved in the event.
     */
    public Island getIsland() {
        return island;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
