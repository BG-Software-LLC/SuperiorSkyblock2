package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;

/**
 * IslandSchematicPasteEvent is called when a schematic is placed.
 */
public class IslandSchematicPasteEvent extends IslandEvent {

    private final String schematic;
    private final Location location;

    /**
     * The constructor for the event.
     *
     * @param island    The island object that was created.
     * @param schematic The schematic that was used.
     * @param location  The location the schematic was pasted at.
     */
    public IslandSchematicPasteEvent(Island island, String schematic, Location location) {
        super(island);
        this.schematic = schematic;
        this.location = location.clone();
    }

    /**
     * Get the schematic that was used.
     */
    public String getSchematic() {
        return schematic;
    }

    /**
     * Get the location that the schematic was pasted at.
     */
    public Location getLocation() {
        return location;
    }
}
