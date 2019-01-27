package com.ome_r.superiorskyblock.hooks;

import com.ome_r.superiorskyblock.utils.key.Key;
import org.bukkit.Location;

public interface BlocksProvider {

    int getBlockCount(Location location);

    Key getBlockKey(Location location, Key def);

}
