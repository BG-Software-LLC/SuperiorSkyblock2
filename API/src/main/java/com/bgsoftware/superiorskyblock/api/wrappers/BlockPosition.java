package com.bgsoftware.superiorskyblock.api.wrappers;

import com.bgsoftware.common.annotations.Nullable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * This object represents a position of a block in the world.
 * You can create a new instance of this class by using {@link com.bgsoftware.superiorskyblock.api.handlers.FactoriesManager}
 */
public interface BlockPosition {

    /**
     * Get the name of the world of the position.
     */
    @Deprecated
    String getWorldName();

    /**
     * Get the world of the position.
     *
     * @return The world object. May be null if the {@link #getWorldName()} is not a valid world.
     */
    @Nullable
    @Deprecated
    World getWorld();

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
     * Get the block object of that position.
     */
    @Deprecated
    Block getBlock();

    /**
     * Get the location of that position in a specific world.
     */
    @Deprecated
    Location parse(World world);

    /**
     * Get the location of that position in the default world.
     */
    @Deprecated
    Location parse();

}
