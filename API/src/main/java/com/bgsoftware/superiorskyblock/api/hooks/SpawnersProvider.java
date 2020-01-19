package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface SpawnersProvider {

    Pair<Integer, EntityType> getSpawner(Location location);

    EntityType getSpawnerType(ItemStack itemStack);

}
