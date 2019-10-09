package com.bgsoftware.superiorskyblock.api.schematic;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;

public interface Schematic {

    /**
     * Paste te schematic in a specific location.
     * @param location The location to paste the schematic at.
     * @param callback A callback runnable that runs when the process finishes
     *
     * @deprecated See pasteSchematic(Island, Location, Runnable)
     */
    void pasteSchematic(Location location, Runnable callback);

    /**
     * Paste te schematic in a specific location.
     * @param island The island of the schematic.
     * @param location The location to paste the schematic at.
     * @param callback A callback runnable that runs when the process finishes
     */
    void pasteSchematic(Island island, Location location, Runnable callback);

}
