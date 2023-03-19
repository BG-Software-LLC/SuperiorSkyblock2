package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandKickEvent is called when a player is kicked from his island.
 */
public class IslandKickEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final SuperiorPlayer targetPlayer;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player who kicked the other player. If null, it means console kicked the player.
     * @param targetPlayer   The player that was kicked.
     * @param island         The island that the player was kicked from.
     */
    public IslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.targetPlayer = targetPlayer;
    }

    /**
     * Get the player who kicked the other player.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the player that was kicked.
     */
    public SuperiorPlayer getTarget() {
        return targetPlayer;
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
