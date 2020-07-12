package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.Location;

public final class BlocksProvider_Default implements BlocksProvider {

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        return new Pair<>(1, null);
    }

}
