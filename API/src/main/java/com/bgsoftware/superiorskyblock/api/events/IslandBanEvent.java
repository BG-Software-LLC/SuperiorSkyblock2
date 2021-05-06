package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class IslandBanEvent extends IslandEvent {

    private final SuperiorPlayer superiorPlayer, targetPlayer;

    /**
     * The constructor for the event.
     *
     * @param island The island object that was involved in the event.
     */
    public IslandBanEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island) {
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
