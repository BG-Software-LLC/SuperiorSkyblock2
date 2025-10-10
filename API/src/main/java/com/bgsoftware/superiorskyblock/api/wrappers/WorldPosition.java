package com.bgsoftware.superiorskyblock.api.wrappers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.handlers.FactoriesManager;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * This object represents a position of an entity in the world.
 * You can create a new instance of this class by using {@link FactoriesManager}
 */
public interface WorldPosition {

    /**
     * Get the x value of the position.
     */
    double getX();

    /**
     * Get the y value of the position.
     */
    double getY();

    /**
     * Get the z value of the position.
     */
    double getZ();

    /**
     * Get the yaw value of the position.
     */
    float getYaw();

    /**
     * Get the pitch value of the position.
     */
    float getPitch();

    /**
     * Get a new position by an offset from this position.
     *
     * @param x The x-axis offset.
     * @param y The y-axis offset.
     * @param z The z-axis offset.
     */
    WorldPosition offset(double x, double y, double z);

    /**
     * Get a new position by rotating this position.
     *
     * @param yaw   The y-axis rotation.
     * @param pitch The z-axis rotation.
     */
    WorldPosition rotate(float yaw, float pitch);

    /**
     * Get a new position by an offset from this position.
     *
     * @param x     The x-axis offset.
     * @param y     The y-axis offset.
     * @param z     The z-axis offset.
     * @param yaw   The y-axis rotation.
     * @param pitch The z-axis rotation.
     */
    WorldPosition offset(double x, double y, double z, float yaw, float pitch);

    /**
     * Get the bukkit representation of this world position in the provided world.
     *
     * @param world The world for this world position.
     */
    Location toLocation(@Nullable World world);

    /**
     * Get the bukkit representation of this world position in the provided world.
     *
     * @param world    The world for this world position.
     * @param location The location to write the output to. If null, null will be returned as well.
     */
    @Nullable
    Location toLocation(@Nullable World world, @Nullable Location location);

    /**
     * Get the bukkit representation of this world position in the provided world info.
     * If the world is unloaded, the location's getWorld will return null.
     *
     * @param worldInfo The world information for this world position.
     */
    Location toLocation(WorldInfo worldInfo);

    /**
     * Get the bukkit representation of this world position in the provided world info.
     * If the world is unloaded, the location's getWorld will return null.
     *
     * @param worldInfo The world information for this world position.
     * @param location  The location to write the output to. If null, null will be returned as well.
     */
    @Nullable
    Location toLocation(WorldInfo worldInfo, @Nullable Location location);

    /**
     * Get the block position representation of this world position.
     */
    BlockPosition toBlockPosition();

}
