package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

/**
 * IslandLeaveProtectedEvent is called when a player is walking out from the island's protected area.
 * The protected area is the area that players can build in.
 */
public class IslandLeaveProtectedEvent extends IslandLeaveEvent {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player who left the island's protected area.
     * @param island         The island that the player left.
     * @param leaveCause     The cause of leaving the island.
     * @param toLocation     The location the player will be at after leaving.
     */
    public IslandLeaveProtectedEvent(SuperiorPlayer superiorPlayer, Island island, LeaveCause leaveCause, Location toLocation) {
        super(superiorPlayer, island, leaveCause, toLocation);
    }

    public static HandlerList getHandlerList() {
        return handlers;
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

}
