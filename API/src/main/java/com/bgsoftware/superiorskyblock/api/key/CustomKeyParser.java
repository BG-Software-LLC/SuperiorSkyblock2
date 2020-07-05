package com.bgsoftware.superiorskyblock.api.key;

import org.bukkit.Location;

public interface CustomKeyParser {

    Key getCustomKey(Location location);

    boolean isCustomKey(Key key);

}
