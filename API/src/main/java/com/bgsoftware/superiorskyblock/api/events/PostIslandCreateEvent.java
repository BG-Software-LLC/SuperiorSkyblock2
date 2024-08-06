package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

/**
 * IslandCreateEvent is called after the island is created and the player was teleported to it.
 */
public class PostIslandCreateEvent extends IslandEvent {

    private final SuperiorPlayer superiorPlayer;

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who created the island.
     * @param island         The island object that was created.
     */
    public PostIslandCreateEvent(SuperiorPlayer superiorPlayer, Island island) {
        super(island);
        this.superiorPlayer = superiorPlayer;
    }

    /**
     * Get the player who created the island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

}
