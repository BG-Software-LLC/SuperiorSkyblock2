package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandQuitEvent is called when a player is leaving their island.
 */
public class IslandQuitEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player who left their island.
     * @param island         The island that the player left.
     */
    public IslandQuitEvent(SuperiorPlayer superiorPlayer, Island island) {
        super(island);
        this.superiorPlayer = superiorPlayer;
    }

    /**
     * Get the player who left their island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
