package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

/**
 * IslandSetVisitorHomeEvent is called when a new visitor home is set to the island.
 */
public class IslandSetVisitorHomeEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;

    private Location islandVisitorHome;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer    The player that changed the island visitor home.
     * @param island            The island that the visitor home was changed for.
     * @param islandVisitorHome The new island visitor home of the island.
     */
    public IslandSetVisitorHomeEvent(SuperiorPlayer superiorPlayer, Island island, Location islandVisitorHome) {
        super(island);
        this.islandVisitorHome = islandVisitorHome.clone();
        this.superiorPlayer = superiorPlayer;
    }

    /**
     * Get the player who changed the island home, if exists.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new island visitor home location of the island.
     */
    public Location getIslandVisitorHome() {
        return islandVisitorHome.clone();
    }

    /**
     * Set the new visitor home location of the island.
     * Setting the visitor home location outside the island's area may lead to undefined behaviors.
     *
     * @param islandVisitorHome The new home visitor location for the island.
     */
    public void setIslandHome(Location islandVisitorHome) {
        Preconditions.checkNotNull(islandVisitorHome.getWorld(), "Cannot set island visitor home with null world");
        this.islandVisitorHome = islandVisitorHome.clone();
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
