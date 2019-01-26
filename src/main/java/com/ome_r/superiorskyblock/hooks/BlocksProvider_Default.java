package com.ome_r.superiorskyblock.hooks;

import com.ome_r.superiorskyblock.SuperiorSkyblock;
import org.bukkit.Location;

public class BlocksProvider_Default implements BlocksProvider {

    private static SuperiorSkyblock plugin = SuperiorSkyblock.getPlugin();

    @Override
    public int getBlockCount(Location location) {
        return plugin.getGrid().getBlockAmount(location.getBlock());
    }
}
