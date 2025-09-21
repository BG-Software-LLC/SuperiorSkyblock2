package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandWarpTeleportEvent is called when a player teleports to island visitor home.
 */
public class IslandVisitorHomeTeleportEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final Dimension dimension;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island that the player teleports to.
     * @param superiorPlayer The player who teleports to the island visitor home.
     * @param dimension      The dimension that the player teleports to.
     */
    public IslandVisitorHomeTeleportEvent(Island island, SuperiorPlayer superiorPlayer, Dimension dimension) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.dimension = dimension;
    }

    /**
     * Get the player who teleports to the island visitor home.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the dimension that the player teleports to.
     */
    public Dimension getDimension() {
        return this.dimension;
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
