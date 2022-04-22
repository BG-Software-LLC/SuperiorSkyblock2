package com.bgsoftware.superiorskyblock.api.wrappers;

import org.bukkit.Location;

/**
 * This object represents an offset from a block.
 * You can create a new instance of this class by using {@link com.bgsoftware.superiorskyblock.api.handlers.FactoriesManager}
 */
public interface BlockOffset {

    /**
     * Get the x-coords offset.
     */
    int getOffsetX();

    /**
     * Get the y-coords offset.
     */
    int getOffsetY();

    /**
     * Get the z-coords offset.
     */
    int getOffsetZ();

    /**
     * Apply this block-offset to a location.
     *
     * @param location The location to apply the offset to.
     * @return A new copy of the location with the offset applied to it.
     */
    Location applyToLocation(Location location);

}
