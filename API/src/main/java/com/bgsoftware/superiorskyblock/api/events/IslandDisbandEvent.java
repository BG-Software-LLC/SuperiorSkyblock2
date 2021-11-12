package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandDisbandEvent is called when an island is disbanded.
 */
public class IslandDisbandEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who proceed the operation.
     * @param island         The island that is being disbanded.
     */
    public IslandDisbandEvent(SuperiorPlayer superiorPlayer, Island island) {
        super(island);
        this.superiorPlayer = superiorPlayer;
    }

    /**
     * Get the player who proceed the operation.
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
