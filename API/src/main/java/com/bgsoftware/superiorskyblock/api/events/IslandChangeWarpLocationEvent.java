package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeWarpLocationEvent is called when the location of a warp was changed.
 */
public class IslandChangeWarpLocationEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final IslandWarp islandWarp;

    private Location location;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the location of the warp.
     * @param island         The island of the warp.
     * @param islandWarp     The warp that its location was changed.
     * @param location       The new location of the warp.
     */
    public IslandChangeWarpLocationEvent(SuperiorPlayer superiorPlayer, Island island, IslandWarp islandWarp, Location location) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.islandWarp = islandWarp;
        this.location = location.clone();
    }

    /**
     * Get the player that changed the location of the warp.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the warp that its location was changed.
     */
    public IslandWarp getIslandWarp() {
        return islandWarp;
    }

    /**
     * Get the new location of the warp.
     */
    public Location getLocation() {
        return location.clone();
    }

    /**
     * Set the new location for the warp.
     *
     * @param location The new location to set.
     */
    public void setLocation(Location location) {
        Preconditions.checkNotNull(location, "Cannot set warp location to null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        Preconditions.checkState(island.isInsideRange(location), "Warp locations must be inside the island's area.");

        this.location = location.clone();
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
