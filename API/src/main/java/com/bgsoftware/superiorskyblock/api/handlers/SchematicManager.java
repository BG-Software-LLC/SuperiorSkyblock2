package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.SchematicOptions;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;

import java.util.List;

public interface SchematicManager {

    /**
     * Get a schematic by it's name.
     *
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
     * Register a new schematic parser.
     * Files will be parsed using the registered schematics. The plugin will attempt to parse the schematic files
     * using each of the parsers in the same order they were registered. If no parsers are available, or no parser
     * could parse the file, the plugin will use the default parser.
     *
     * @param schematicParser The schematic-parser to register.
     */
    void registerSchematicParser(SchematicParser schematicParser);

    /**
     * Get all the registered parsers, in the same order they were registered.
     */
    List<SchematicParser> getSchematicParsers();

    /**
     * Save a schematic.
     * Calls the {@link #saveSchematic(SuperiorPlayer, String, boolean)} with saveAir set to false.
     *
     * @param superiorPlayer The player who saves the schematic.
     * @param schematicName  The schematic name.
     */
    void saveSchematic(SuperiorPlayer superiorPlayer, String schematicName);

    /**
     * Save a schematic.
     * Calls the {@link #saveSchematic(Location, Location, SchematicOptions, Runnable)} method with default values.
     *
     * @param superiorPlayer The player who saves the schematic.
     * @param schematicName  The schematic name.
     * @param saveAir        Whether to save air blocks into the schematic.
     */
    void saveSchematic(SuperiorPlayer superiorPlayer, String schematicName, boolean saveAir);

    /**
     * Save a schematic.
     *
     * @param pos1          First position for the schematic.
     * @param pos2          Second position for the schematic.
     * @param offsetX       The offset x value for the schematic (from minimum location between the two)
     * @param offsetY       The offset y value for the schematic (from minimum location between the two)
     * @param offsetZ       The offset z value for the schematic (from minimum location between the two)
     * @param schematicName The new schematic name that will be created.
     */
    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, String schematicName);

    /**
     * Save a schematic.
     *
     * @param pos1          First position for the schematic.
     * @param pos2          Second position for the schematic.
     * @param offsetX       The offset x value for the schematic (from minimum location between the two)
     * @param offsetY       The offset y value for the schematic (from minimum location between the two)
     * @param offsetZ       The offset z value for the schematic (from minimum location between the two)
     * @param yaw           The yaw value of the schematic.
     * @param pitch         The pitch value of the schematic.
     * @param schematicName The new schematic name that will be created.
     */
    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, float yaw, float pitch, String schematicName);

    /**
     * Save a schematic.
     *
     * @param pos1          First position for the schematic.
     * @param pos2          Second position for the schematic.
     * @param offsetX       The offset x value for the schematic (from minimum location between the two)
     * @param offsetY       The offset y value for the schematic (from minimum location between the two)
     * @param offsetZ       The offset z value for the schematic (from minimum location between the two)
     * @param schematicName The new schematic name that will be created.
     * @param callable      A runnable that will be ran after the task is completed.
     */
    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, String schematicName, Runnable callable);

    /**
     * Save a schematic.
     *
     * @param pos1          First position for the schematic.
     * @param pos2          Second position for the schematic.
     * @param offsetX       The offset x value for the schematic (from minimum location between the two)
     * @param offsetY       The offset y value for the schematic (from minimum location between the two)
     * @param offsetZ       The offset z value for the schematic (from minimum location between the two)
     * @param yaw           The yaw value of the schematic.
     * @param pitch         The pitch value of the schematic.
     * @param schematicName The new schematic name that will be created.
     * @param callable      A runnable that will be ran after the task is completed.
     */
    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, float yaw, float pitch, String schematicName, @Nullable Runnable callable);

    /**
     * Save a schematic.
     *
     * @param pos1             First position for the schematic.
     * @param pos2             Second position for the schematic.
     * @param schematicOptions The options for creating the new schematic.
     */
    void saveSchematic(Location pos1, Location pos2, SchematicOptions schematicOptions);

    /**
     * Save a schematic.
     *
     * @param pos1             First position for the schematic.
     * @param pos2             Second position for the schematic.
     * @param schematicOptions The options for creating the new schematic.
     * @param callable         A runnable that will be ran after the task is completed.
     */
    void saveSchematic(Location pos1, Location pos2, SchematicOptions schematicOptions, @Nullable Runnable callable);

}
