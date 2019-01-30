package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Location;

public interface BlocksProvider {

    int getBlockCount(Location location);

    Key getBlockKey(Location location, Key def);

}
