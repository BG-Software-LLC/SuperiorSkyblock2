package com.bgsoftware.superiorskyblock.api.wrappers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.handlers.FactoriesManager;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * This object represents a position of a block in the world.
 * You can create a new instance of this class by using {@link FactoriesManager}
 */
public interface BlockPosition {

    /**
     * Get the x value of the position.
     */
    int getX();

    /**
     * Get the y value of the position.
     */
    int getY();

    /**
     * Get the z value of the position.
     */
    int getZ();

    /**
     * Get a new position by an offset from this position.
     *
     * @param x The x-axis offset.
     * @param y The y-axis offset.
     * @param z The z-axis offset.
     */
    BlockPosition offset(int x, int y, int z);

    /**
     * Get the bukkit representation of this block position in the provided world.
     *
     * @param world The world for this block position.
     */
    Location toLocation(@Nullable World world);

    /**
     * Get the bukkit representation of this block position in the provided world.
     *
     * @param world    The world for this block position.
     * @param location The location to write the output to. If null, null will be returned as well.
     */
    @Nullable
    Location toLocation(@Nullable World world, @Nullable Location location);

    /**
     * Get the bukkit representation of this block position in the provided world.
     * If the world is unloaded, the location's getWorld will return null.
     *
     * @param worldInfo The world information for this world position.
     */
    Location toLocation(WorldInfo worldInfo);

    /**
     * Get the bukkit representation of this block position in the provided world.
     * If the world is unloaded, the location's getWorld will return null.
     *
     * @param worldInfo The world information for this world position.
     * @param location  The location to write the output to. If null, null will be returned as well.
     */
    @Nullable
    Location toLocation(WorldInfo worldInfo, @Nullable Location location);

    /**
     * Get the world position representation of this block position.
     */
    WorldPosition toWorldPosition();

}
