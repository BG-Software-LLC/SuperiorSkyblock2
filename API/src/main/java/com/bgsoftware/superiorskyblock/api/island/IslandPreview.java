package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.GameMode;
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
     * Get the location of the island preview.
     *
     * @param location Location object to re-use.
     */
    @Nullable
    Location getLocation(@Nullable Location location);

    /**
     * Get the requested schematic.
     */
    String getSchematic();

    /**
     * Get the island name that was requested.
     */
    String getIslandName();

    /**
     * Get the game mode that the player had before the preview started.
     */
    GameMode getPreviousGameMode();

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
