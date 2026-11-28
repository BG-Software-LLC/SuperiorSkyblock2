package com.bgsoftware.superiorskyblock.api.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;

import java.io.InputStream;

/**
 * A schematic parser that parses schematics from the raw bytes of their files.
 * The stream is not decompressed: parsers of compressed formats must decompress it themselves.
 * The stream is closed by the caller.
 */
public interface StreamSchematicParser extends SchematicParser {

    /**
     * Parse a schematic from the raw bytes of its file.
     *
     * @param inputStream   The raw stream of the schematic file.
     * @param schematicName The name of the schematic.
     * @return A new {@link Schematic} object from the stream.
     * @throws SchematicParseException If the stream doesn't meet the format of this parser.
     */
    Schematic parseSchematic(InputStream inputStream, String schematicName) throws SchematicParseException;

}
