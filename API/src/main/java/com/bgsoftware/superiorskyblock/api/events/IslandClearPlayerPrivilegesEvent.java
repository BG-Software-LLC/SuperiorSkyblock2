package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandClearPlayerPrivilegesEvent is called when privileges of a player is cleared on an island.
 */
public class IslandClearPlayerPrivilegesEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final SuperiorPlayer privilegedPlayer;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island           The island that the privileges were cleared in.
     * @param superiorPlayer   The player that cleared the privileges to the other player.
     * @param privilegedPlayer The player that the privileges were cleared for.
     */
    public IslandClearPlayerPrivilegesEvent(Island island, SuperiorPlayer superiorPlayer, SuperiorPlayer privilegedPlayer) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.privilegedPlayer = privilegedPlayer;
    }

    /**
     * Get the player that cleared the privileges to the other player.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the player that the privileges were cleared for.
     */
    public SuperiorPlayer getPrivilegedPlayer() {
        return privilegedPlayer;
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
