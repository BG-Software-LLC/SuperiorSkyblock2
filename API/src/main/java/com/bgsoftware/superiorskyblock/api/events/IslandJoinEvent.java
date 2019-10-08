package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandJoinEvent is called when a player is joining an island as a member of that island.
 */
public class IslandJoinEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final Island island;
    private boolean cancelled = false;

    /**
     * The constructor to the event.
     * @param superiorPlayer The player who joined the island as a new member.
     * @param island The island that the player joined into.
     */
    public IslandJoinEvent(SuperiorPlayer superiorPlayer, Island island){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    /**
     * Get the player who joined the island as a new member.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the island that the player joined into.
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
