package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandDisbandEvent is called when an island is disbanded.
 */
public class IslandDisbandEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final Island island;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     * @param superiorPlayer The player who proceed the operation.
     * @param island The island that is being disbanded.
     */
    public IslandDisbandEvent(SuperiorPlayer superiorPlayer, Island island){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    /**
     * Get the player who proceed the operation.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the island that is being disbanded.
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
