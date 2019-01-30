package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Location;

public final class BlocksProvider_Default implements BlocksProvider {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public int getBlockCount(Location location) {
        return plugin.getGrid().getBlockAmount(location);
    }

    @Override
    public Key getBlockKey(Location location, Key def) {
        return def;
    }
}
