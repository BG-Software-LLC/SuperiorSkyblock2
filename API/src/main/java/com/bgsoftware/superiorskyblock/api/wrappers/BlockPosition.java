package com.bgsoftware.superiorskyblock.api.wrappers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public interface BlockPosition {

    /**
     * Get the world of the position.
     */
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
     * Get the block object of that position.
     */
    Block getBlock();

    /**
     * Get the location of that position in a specific world.
     */
    Location parse(World world);

    /**
     * Get the location of that position in the default world.
     */
    Location parse();

}
