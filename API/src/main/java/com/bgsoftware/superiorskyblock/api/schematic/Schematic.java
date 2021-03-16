package com.bgsoftware.superiorskyblock.api.schematic;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface Schematic {

    /**
     * Get the name of the schematic.
     */
    String getName();

    /**
     * Paste te schematic in a specific location.
     * @param island The island of the schematic.
     * @param location The location to paste the schematic at.
     * @param callback A callback runnable that runs when the process finishes
     */
    void pasteSchematic(Island island, Location location, Runnable callback);

    /**
     * Paste te schematic in a specific location.
     * @param island The island of the schematic.
     * @param location The location to paste the schematic at.
     * @param callback A callback runnable that runs when the process finishes
     * @param onFailure A consumer that will be ran if the creation fails.
     */
    void pasteSchematic(Island island, Location location, Runnable callback, @Nullable Consumer<Throwable> onFailure);

}
