package com.bgsoftware.superiorskyblock.api.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;

import java.io.DataInputStream;

public interface SchematicParser {

    /**
     * Parse a schematic from a stream.
     *
     * @param inputStream   The stream to parse the schematic from.
     * @param schematicName The name of the schematic.
     * @return A new {@link Schematic} object from the stream.
     * @throws SchematicParseException If the stream doesn't meet the format of this parser.
     */
    Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException;

}
