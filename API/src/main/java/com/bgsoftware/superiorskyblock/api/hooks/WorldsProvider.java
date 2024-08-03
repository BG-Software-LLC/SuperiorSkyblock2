package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public interface WorldsProvider {

    /**
     * Prepare all the island worlds on startup.
     */
    void prepareWorlds();

    /**
     * Get the world of an island by the dimension.
     * If the world is not loaded, this method should load the world before returning.
     *
     * @param dimension The world dimension.
     * @param island    The island to check.
     */
    @Nullable
    World getIslandsWorld(Island island, Dimension dimension);

    /**
     * Get the world of an island by the environment.
     * If the world is not loaded, this method should load the world before returning.
     *
     * @param environment The world environment.
     * @param island      The island to check.
     */
    @Deprecated
    @Nullable
    default World getIslandsWorld(Island island, World.Environment environment) {
        return getIslandsWorld(island, Dimension.getByName(environment.name()));
    }

    /**
     * Get the dimension of an islands world.
     * If the island is not an islands world, null will be returned.
     *
     * @param world The world to check.
     */
    @Nullable
    Dimension getIslandsWorldDimension(World world);

    /**
     * Checks if the given world is an islands world.
     *
     * @param world The world to check.
     */
    boolean isIslandsWorld(World world);

    /**
     * Get the location for a new island that is created.
     *
     * @param previousLocation The location of the previous island that was created.
     * @param islandsHeight    The default islands height.
     * @param maxIslandSize    The default maximum island size.
     * @param islandOwner      The owner of the island.
     * @param islandUUID       The UUID of the island.
     */
    Location getNextLocation(Location previousLocation, int islandsHeight, int maxIslandSize, UUID islandOwner, UUID islandUUID);

    /**
     * Callback upon finishing of creation of islands.
     *
     * @param islandLocation The location of the new island.
     * @param islandOwner    The owner of the island.
     * @param islandUUID     The UUID of the island.
     */
    void finishIslandCreation(Location islandLocation, UUID islandOwner, UUID islandUUID);

    /**
     * Prepare teleportation of an entity to an island.
     *
     * @param island         The target island.
     * @param location       The location that the entity will be teleported to.
     * @param finishCallback Callback function after the preparation is finished.
     */
    void prepareTeleport(Island island, Location location, Runnable finishCallback);

    /**
     * Check whether or not normal worlds are enabled.
     */
    default boolean isNormalEnabled() {
        return isDimensionEnabled(Dimension.getByName("NORMAL"));
    }

    /**
     * Check whether or not normal worlds are unlocked for islands by default.
     */
    default boolean isNormalUnlocked() {
        return isDimensionUnlocked(Dimension.getByName("NORMAL"));
    }

    /**
     * Check whether or not nether worlds are enabled.
     */
    default boolean isNetherEnabled() {
        return isDimensionEnabled(Dimension.getByName("NETHER"));
    }

    /**
     * Check whether or not nether worlds are unlocked for islands by default.
     */
    default boolean isNetherUnlocked() {
        return isDimensionUnlocked(Dimension.getByName("NETHER"));
    }

    /**
     * Check whether or not end worlds are enabled.
     */
    default boolean isEndEnabled() {
        return isDimensionEnabled(Dimension.getByName("THE_END"));
    }

    /**
     * Check whether or not end worlds are unlocked for islands by default.
     */
    default boolean isEndUnlocked() {
        return isDimensionUnlocked(Dimension.getByName("THE_END"));
    }

    /**
     * Check whether a dimension is enabled on the server.
     *
     * @param dimension The dimension to check.
     */
    boolean isDimensionEnabled(Dimension dimension);

    /**
     * Check whether a dimension is unlocked for islands by default.
     */
    boolean isDimensionUnlocked(Dimension dimension);

}
