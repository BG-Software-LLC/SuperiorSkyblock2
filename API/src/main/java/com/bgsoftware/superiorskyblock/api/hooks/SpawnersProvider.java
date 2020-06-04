package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface SpawnersProvider {

    /**
     * Get a pair that represents information about a spawner in a specific location.
     * This method is called async first, and if the string in the pair is null, it will be called synced later.
     * The integer represents the amount of spawners in that location, and the string represents the entity type.
     * @param location The location to check.
     */
    Pair<Integer, String> getSpawner(Location location);

    /**
     * Get the spawner type from an item.
     * @param itemStack The item to check.
     */
    String getSpawnerType(ItemStack itemStack);

}
