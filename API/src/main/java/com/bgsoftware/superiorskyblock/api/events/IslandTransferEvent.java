package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandTransferEvent is called when the leadership of an island is transferred.
 */
public class IslandTransferEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer oldOwner, newOwner;
    private final Island island;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     * @param island The island that the leadership of it is transferred.
     * @param oldOwner The old owner of the island.
     * @param newOwner The new owner of the island.
     */
    public IslandTransferEvent(Island island, SuperiorPlayer oldOwner, SuperiorPlayer newOwner){
        this.island = island;
        this.oldOwner = oldOwner;
        this.newOwner = newOwner;
    }

    /**
     * Get the old owner of the island.
     */
    public SuperiorPlayer getOldOwner() {
        return oldOwner;
    }

    /**
     * Get the new owner of the island.
     */
    public SuperiorPlayer getNewOwner() {
        return newOwner;
    }

    /**
     * Get the island that the leadership of it is transferred.
     */
    public Island getIsland() {
        return island;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
