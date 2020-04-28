package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface SpawnersProvider {

    Pair<Integer, String> getSpawner(Location location);

    String getSpawnerType(ItemStack itemStack);

}
