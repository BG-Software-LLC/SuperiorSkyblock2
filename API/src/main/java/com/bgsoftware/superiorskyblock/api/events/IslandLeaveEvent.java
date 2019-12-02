package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandLeaveEvent is called when a player is walking out from the island's area.
 */
public class IslandLeaveEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final LeaveCause leaveCause;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who left the island's area.
     * @param island The island that the player left.
     *
     * @deprecated See IslandLeaveEvent(SuperiorPlayer, Island, LeaveCause)
     */
    public IslandLeaveEvent(SuperiorPlayer superiorPlayer, Island island){
        this(superiorPlayer, island, LeaveCause.INVALID);
    }

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who left the island's area.
     * @param island The island that the player left.
     * @param leaveCause The cause of leaving the island.
     */
    public IslandLeaveEvent(SuperiorPlayer superiorPlayer, Island island, LeaveCause leaveCause){
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.leaveCause = leaveCause;
    }

    /**
     * Get the player who left the island's area.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the cause of leaving the island.
     */
    public LeaveCause getCause() {
        return leaveCause;
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
     * Used to determine the cause of leaving.
     */
    public enum LeaveCause{

        PLAYER_MOVE,
        PLAYER_TELEPORT,
        PLAYER_QUIT,
        INVALID

    }

}
