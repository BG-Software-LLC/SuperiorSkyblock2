package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;


/**
 * IslandSetHomeEvent is called when a new home is set to the island.
 */
public class IslandSetHomeEvent extends IslandEvent implements Cancellable {

    private final Reason reason;
    @Nullable
    private final SuperiorPlayer superiorPlayer;

    private Location islandHome;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island that the home was changed for.
     * @param islandHome     The new island home of the island.
     * @param reason         The reason the home was changed.
     * @param superiorPlayer The player that changed the island home, if exists
     */
    public IslandSetHomeEvent(Island island, Location islandHome, Reason reason, @Nullable SuperiorPlayer superiorPlayer) {
        super(island);
        this.islandHome = islandHome.clone();
        this.reason = reason;
        this.superiorPlayer = superiorPlayer;
    }

    /**
     * Get the new island home location of the island.
     */
    public Location getIslandHome() {
        return islandHome.clone();
    }

    /**
     * Set the new home location of the island.
     *
     * @param islandHome The home location for the island.
     */
    public void setIslandHome(Location islandHome) {
        Preconditions.checkNotNull(islandHome.getWorld(), "Cannot set island home with null world");
        this.islandHome = islandHome.clone();
    }

    /**
     * Get the reason the home was changed.
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Get the player who changed the island home, if exists.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * The reason the home was changed.
     */
    public enum Reason {

        /**
         * The home was changed through a command.
         */
        SET_HOME_COMMAND,

        /**
         * The home was changed because the old home was not safe.
         */
        SAFE_HOME

    }

}
