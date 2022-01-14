package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandTransferEvent is called when the leadership of an island is transferred.
 */
public class IslandTransferEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer oldOwner;
    private final SuperiorPlayer newOwner;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island   The island that the leadership of it is transferred.
     * @param oldOwner The old owner of the island.
     * @param newOwner The new owner of the island.
     */
    public IslandTransferEvent(Island island, SuperiorPlayer oldOwner, SuperiorPlayer newOwner) {
        super(island);
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
