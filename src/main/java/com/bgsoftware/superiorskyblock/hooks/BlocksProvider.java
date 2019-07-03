package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.utils.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public interface BlocksProvider {

    Pair<Integer, EntityType> getSpawner(Location location);

    default Pair<Integer, Material> getBlock(Location location){
        return null;
    }

}
