package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.HandlerList;

/**
 * IslandEnterProtectedEvent is called when a player is walking into an island's protected area.
 * The protected area is the area that players can build in.
 */
public class IslandEnterProtectedEvent extends IslandEnterEvent {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player who entered to the island's area.
     * @param island         The island that the player entered into.
     * @param enterCause     The cause of entering into the island.
     */
    public IslandEnterProtectedEvent(SuperiorPlayer superiorPlayer, Island island, EnterCause enterCause) {
        super(superiorPlayer, island, enterCause);
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
