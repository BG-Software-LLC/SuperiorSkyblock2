package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandCoopPlayerEvent is called when a player is making another player coop on their island.
 */
public class IslandCoopPlayerEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer player;
    private final SuperiorPlayer target;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island The island that the leadership of it is transferred.
     * @param player The player who cooped the target.
     * @param target The player that will be cooped.
     */
    public IslandCoopPlayerEvent(Island island, SuperiorPlayer player, SuperiorPlayer target) {
        super(island);
        this.player = player;
        this.target = target;
    }

    /**
     * Get the player who cooped the target.
     */
    public SuperiorPlayer getPlayer() {
        return player;
    }

    /**
     * Get the player that will be cooped.
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
