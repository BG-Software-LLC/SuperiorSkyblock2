package com.bgsoftware.superiorskyblock.api.schematic;

import org.bukkit.Location;

public interface Schematic {

    void pasteSchematic(Location location, Runnable callback);

}
