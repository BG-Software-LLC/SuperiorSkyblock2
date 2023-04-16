package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandCoopRemovePlayerEvent is called when a player is removing another player from coop on their island.
 */
public class IslandCoopRemovePlayerEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer player;
    private final SuperiorPlayer target;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island The island that the leadership of it is transferred.
     * @param player The player who de-cooped the target.
     * @param target The player that will be removed.
     */
    public IslandCoopRemovePlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target) {
        super(island);
        this.player = player;
        this.target = target;
    }

    /**
     * Get the player who removed the cooped target
     */
    public SuperiorPlayer getPlayer() {
        return player;
    }

    /**
     * Get the player that will be removed from coop.
     */
    public SuperiorPlayer getTarget() {
        return target;
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
