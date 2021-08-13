package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

/**
 * IslandBanEvent is called when a player is banned from his island.
 */
public class IslandBanEvent extends IslandEvent {

    private final SuperiorPlayer superiorPlayer, targetPlayer;

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who banned the other player.
     * @param targetPlayer The player that was banned.
     * @param island The island that the player was banned from.
     */
    public IslandBanEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.targetPlayer = targetPlayer;
    }

    /**
     * Get the player who banned the other player.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the player that was banned.
     */
    public SuperiorPlayer getTarget() {
        return targetPlayer;
    }

}
