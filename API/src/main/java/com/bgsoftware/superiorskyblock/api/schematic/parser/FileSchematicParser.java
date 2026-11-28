package com.bgsoftware.superiorskyblock.api.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;

import java.io.File;

/**
 * A schematic parser that parses schematics directly from their files.
 */
public interface FileSchematicParser extends SchematicParser {

    /**
     * Parse a schematic from its file.
     *
     * @param file          The schematic file to parse.
     * @param schematicName The name of the schematic.
     * @return A new {@link Schematic} object from the file.
     * @throws SchematicParseException If the file doesn't meet the format of this parser.
     */
    Schematic parseSchematic(File file, String schematicName) throws SchematicParseException;

}
