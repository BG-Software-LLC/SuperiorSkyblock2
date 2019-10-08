package com.bgsoftware.superiorskyblock.api.schematic;

import org.bukkit.Location;

public interface Schematic {

    /**
     * Paste te schematic in a specific location.
     * @param location The location to paste the schematic at.
     * @param callback A callback runnable that runs when the process finishes
     */
    void pasteSchematic(Location location, Runnable callback);

}
