package com.bgsoftware.superiorskyblock.hooks.types.defaults;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider;
import org.bukkit.Location;

public final class BlocksProvider_Default implements BlocksProvider {

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        return new Pair<>(1, null);
    }

}
