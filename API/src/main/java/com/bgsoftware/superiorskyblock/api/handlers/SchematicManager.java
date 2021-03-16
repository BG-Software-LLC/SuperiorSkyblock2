package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("unused")
public interface SchematicManager {

    /**
     * Get a schematic by it's name.
     * @param name The name to check.
     * @return The schematic with that name.
     */
    @Nullable
    Schematic getSchematic(String name);

    /**
     * Get a list of all the schematics.
     */
    List<String> getSchematics();

    /**
     * Save a schematic.
     * Calls the saveSchematic(Location, Location, Integer, Integer, Integer, String, Runnable) method with default values.
     * @param superiorPlayer The player who saves the schematic.
     * @param schematicName The schematic name.
     */
    void saveSchematic(SuperiorPlayer superiorPlayer, String schematicName);

    /**
     * Save a schematic.
     * @param pos1 First position for the schematic.
     * @param pos2 Second position for the schematic.
     * @param offsetX The offset x value for the schematic (from minimum location between the two)
     * @param offsetY The offset y value for the schematic (from minimum location between the two)
     * @param offsetZ The offset z value for the schematic (from minimum location between the two)
     * @param schematicName The new schematic name that will be created.
     */
    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, String schematicName);

    /**
     * Save a schematic.
     * @param pos1 First position for the schematic.
     * @param pos2 Second position for the schematic.
     * @param offsetX The offset x value for the schematic (from minimum location between the two)
     * @param offsetY The offset y value for the schematic (from minimum location between the two)
     * @param offsetZ The offset z value for the schematic (from minimum location between the two)
     * @param yaw The yaw value of the schematic.
     * @param pitch The pitch value of the schematic.
     * @param schematicName The new schematic name that will be created.
     */
    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, float yaw, float pitch, String schematicName);

    /**
     * Save a schematic.
     * @param pos1 First position for the schematic.
     * @param pos2 Second position for the schematic.
     * @param offsetX The offset x value for the schematic (from minimum location between the two)
     * @param offsetY The offset y value for the schematic (from minimum location between the two)
     * @param offsetZ The offset z value for the schematic (from minimum location between the two)
     * @param schematicName The new schematic name that will be created.
     * @param callable A runnable that will be ran after the task is completed.
     */
    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, String schematicName, Runnable callable);

    /**
     * Save a schematic.
     * @param pos1 First position for the schematic.
     * @param pos2 Second position for the schematic.
     * @param offsetX The offset x value for the schematic (from minimum location between the two)
     * @param offsetY The offset y value for the schematic (from minimum location between the two)
     * @param offsetZ The offset z value for the schematic (from minimum location between the two)
     * @param yaw The yaw value of the schematic.
     * @param pitch The pitch value of the schematic.
     * @param schematicName The new schematic name that will be created.
     * @param callable A runnable that will be ran after the task is completed.
     */
    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, float yaw, float pitch, String schematicName, @Nullable Runnable callable);

}
