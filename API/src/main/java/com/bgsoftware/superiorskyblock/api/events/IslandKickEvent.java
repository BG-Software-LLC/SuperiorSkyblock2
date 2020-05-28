package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

/**
 * IslandKickEvent is called when a player is kicked from his island.
 */
public class IslandKickEvent extends IslandEvent {

    private final SuperiorPlayer superiorPlayer, targetPlayer;

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who kicked the other player. If null, it means console kicked the player.
     * @param targetPlayer The player that was kicked.
     * @param island The island that the player was kicked from.
     */
    public IslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
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

}
