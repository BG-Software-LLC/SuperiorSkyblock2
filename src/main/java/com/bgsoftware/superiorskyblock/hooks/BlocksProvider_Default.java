package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import org.bukkit.Location;

public class BlocksProvider_Default implements BlocksProvider {

    private static SuperiorSkyblock plugin = SuperiorSkyblock.getPlugin();

    @Override
    public int getBlockCount(Location location) {
        return plugin.getGrid().getBlockAmount(location);
    }

    @Override
    public Key getBlockKey(Location location, Key def) {
        return def;
    }
}
