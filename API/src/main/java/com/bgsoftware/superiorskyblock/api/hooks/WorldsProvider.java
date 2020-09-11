package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public interface WorldsProvider {

    /**
     * Prepare all the island worlds on startup.
     * They must be loaded so SSB will be able to load the islands!
     */
    void prepareWorlds();

    /**
     * Get the world of an island by the environment.
     * @param environment The world environment.
     * @param island The island to check.
     */
    World getIslandsWorld(Island island, World.Environment environment);

    /**
     * Checks if the given world is an islands world.
     * @param world The world to check.
     */
    boolean isIslandsWorld(World world);

    /**
     * Get the location for a new island that is created.
     * @param previousLocation The location of the previous island that was created.
     * @param islandsHeight The default islands height.
     * @param maxIslandSize The default maximum island size.
     * @param islandOwner The owner of the island.
     * @param islandUUID The UUID of the island.
     */
    Location getNextLocation(Location previousLocation, int islandsHeight, int maxIslandSize, UUID islandOwner, UUID islandUUID);

    /**
     * Callback upon finishing of creation of islands.
     * @param islandLocation The location of the new island.
     * @param islandOwner The owner of the island.
     * @param islandUUID The UUID of the island.
     */
    void finishIslandCreation(Location islandLocation, UUID islandOwner, UUID islandUUID);

    /**
     * Prepare teleportation of an entity to an island.
     * @param island The target island.
     * @param finishCallback Callback function after the preparation is finished.
     */
    void prepareTeleport(Island island, Runnable finishCallback);

}
