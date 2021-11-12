package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PreIslandCreateEvent is called when a new island is created.
 */
public class PreIslandCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final String islandName;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player who created the island.
     * @param islandName     The name that was given to the island.
     */
    public PreIslandCreateEvent(SuperiorPlayer superiorPlayer, String islandName) {
        this.superiorPlayer = superiorPlayer;
        this.islandName = islandName;
    }

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player who created the island.
     * @deprecated See PreIslandCreateEvent(SuperiorPlayer, String)
     */
    @Deprecated
    public PreIslandCreateEvent(SuperiorPlayer superiorPlayer) {
        this(superiorPlayer, "");
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the player who created the island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the name that was given to the island.
     */
    public String getIslandName() {
        return islandName;
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
