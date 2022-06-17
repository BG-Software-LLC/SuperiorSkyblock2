package com.bgsoftware.superiorskyblock.dependencies.provider;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class SpawnersProvider_Default implements SpawnersProvider_AutoDetect {

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        return new Pair<>(1, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        return "PIG";
    }

}
