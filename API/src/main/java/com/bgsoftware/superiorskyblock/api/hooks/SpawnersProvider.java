package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public interface SpawnersProvider {

    Pair<Integer, EntityType> getSpawner(Location location);

}
