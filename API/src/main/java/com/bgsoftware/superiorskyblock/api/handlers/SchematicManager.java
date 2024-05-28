package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;

import java.util.List;

public interface SchematicManager {

    /**
     * Get a schematic by its name.
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
     * Calls the saveSchematic(Location, Location, Integer, Integer, Integer, String, Runnable) method with default values.
     *
     * @param superiorPlayer The player who saves the schematic.
     * @param schematicName  The schematic name.
     */
    void saveSchematic(SuperiorPlayer superiorPlayer, String schematicName);

    /**
     * Save a schematic.
     *
     * @param pos1          First position for the schematic.
     * @param pos2          Second position for the schematic.
     * @param offset        The offset location for the schematic (from minimum location between the two)
     * @param spawn         The spawn location for the schematic.
     * @param schematicName The new schematic name that will be created.
     */
    void saveSchematic(Location pos1, Location pos2, Location offset, Location spawn, String schematicName);

    /**
     * Save a schematic.
     *
     * @param pos1          First position for the schematic.
     * @param pos2          Second position for the schematic.
     * @param offset        The offset location for the schematic (from minimum location between the two)
     * @param spawn         The spawn location for the schematic.
     * @param schematicName The new schematic name that will be created.
     * @param callable      A runnable that will be run after the task is completed.
     */
    void saveSchematic(Location pos1, Location pos2, Location offset, Location spawn, String schematicName, @Nullable Runnable callable);

}
