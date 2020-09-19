package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;

/**
 * Object that handles the data of the island preview task.
 */
public interface IslandPreview {

    /**
     * Get the player that is inside the preview.
     */
    SuperiorPlayer getPlayer();

    /**
     * Get the location of the island preview.
     */
    Location getLocation();

    /**
     * Get the requested schematic.
     */
    String getSchematic();

    /**
     * Get the island name that was requested.
     */
    String getIslandName();

    /**
     * Handle confirmation of creation of the island.
     */
    void handleConfirm();

    /**
     * Handle cancellation of the creation of the island.
     */
    void handleCancel();

    /**
     * Handle escaping from the area of the preview.
     */
    void handleEscape();

}
