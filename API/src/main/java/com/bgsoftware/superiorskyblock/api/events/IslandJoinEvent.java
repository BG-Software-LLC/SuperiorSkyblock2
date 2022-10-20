package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandJoinEvent is called when a player is joining an island as a member of that island.
 */
public class IslandJoinEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final Cause cause;
    private boolean cancelled = false;

    /**
     * The constructor to the event.
     *
     * @param superiorPlayer The player who joined the island as a new member.
     * @param island         The island that the player joined into.
     * @deprecated See {@link #IslandJoinEvent(SuperiorPlayer, Island, Cause)}
     */
    @Deprecated
    public IslandJoinEvent(SuperiorPlayer superiorPlayer, Island island) {
        this(superiorPlayer, island, Cause.INVITE);
    }

    /**
     * The constructor to the event.
     *
     * @param superiorPlayer The player who joined the island as a new member.
     * @param island         The island that the player joined into.
     * @param cause          The cause of joining the island.
     */
    public IslandJoinEvent(SuperiorPlayer superiorPlayer, Island island, Cause cause) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.cause = cause;
    }

    /**
     * Get the player who joined the island as a new member.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the cause of joining the island.
     */
    public Cause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * The cause of joining an island.
     */
    public enum Cause {

        /**
         * The player accepted an invitation to the island.
         */
        INVITE,

        /**
         * The player was joined due to an admin, either by `/is admin add` or `/is admin join`
         */
        ADMIN

    }

}
