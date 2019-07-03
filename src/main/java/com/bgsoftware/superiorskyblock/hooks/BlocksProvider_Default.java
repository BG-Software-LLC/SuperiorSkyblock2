package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.utils.Pair;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class BlocksProvider_Default implements BlocksProvider {

    @Override
    public Pair<Integer, EntityType> getSpawner(Location location) {
        return new Pair<>(1, null);
    }

}
