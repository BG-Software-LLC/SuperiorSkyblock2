package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;

import java.util.UUID;

public interface WorldsManager {

    /**
     * Get the location for a new island that is created.
     * @param previousLocation The location of the previous island that was created.
     * @param islandsHeight The default islands height.
     * @param maxIslandSize The default maximum island size.
     * @param islandOwner The owner of the island.
     */
    Location getNextLocation(Location previousLocation, int islandsHeight, int maxIslandSize, UUID islandOwner);

    /**
     * Callback upon finishing of creation of islands.
     * @param islandLocation The location of the new island.
     * @param islandOwner The owner of the island.
     */
    void finishIslandCreation(Location islandLocation, UUID islandOwner);

    /**
     * Prepare teleportation of an entity to an island.
     * @param island The target island.
     * @param finishCallback Callback function after the preparation is finished.
     */
    void prepareTeleport(Island island, Runnable finishCallback);

}
