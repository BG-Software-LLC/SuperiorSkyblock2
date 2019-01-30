package com.bgsoftware.superiorskyblock.api.wrappers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public interface BlockPosition {

    World getWorld();

    int getX();

    int getY();

    int getZ();

    Block getBlock();

    Location parse(World world);

    Location parse();

}
