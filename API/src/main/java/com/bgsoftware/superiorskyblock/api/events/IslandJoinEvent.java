package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandJoinEvent is called when a player is joining an island as a member of that island.
 */
public class IslandJoinEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private boolean cancelled = false;

    /**
     * The constructor to the event.
     *
     * @param superiorPlayer The player who joined the island as a new member.
     * @param island         The island that the player joined into.
     */
    public IslandJoinEvent(SuperiorPlayer superiorPlayer, Island island) {
        super(island);
        this.superiorPlayer = superiorPlayer;
    }

    /**
     * Get the player who joined the island as a new member.
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
