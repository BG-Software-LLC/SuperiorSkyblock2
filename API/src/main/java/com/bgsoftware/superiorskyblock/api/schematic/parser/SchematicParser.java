package com.bgsoftware.superiorskyblock.api.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;

import java.io.DataInputStream;

/**
 * Base type of all schematic parsers.
 * Implement one of its sub-types:
 * <ul>
 *     <li>{@link StreamSchematicParser}: parses schematics from the raw bytes of their files.</li>
 *     <li>{@link FileSchematicParser}: parses schematics directly from their files.</li>
 * </ul>
 * Parsers that implement neither are treated as legacy parsers: they are called with
 * {@link #parseSchematic(DataInputStream, String)} and a GZIP-decompressed stream of the file, as before.
 */
public interface SchematicParser {

    /**
     * Parse a schematic from a GZIP-decompressed stream.
     *
     * @param inputStream   The stream to parse the schematic from.
     * @param schematicName The name of the schematic.
     * @return A new {@link Schematic} object from the stream.
     * @throws SchematicParseException       If the stream doesn't meet the format of this parser.
     * @throws UnsupportedOperationException If the parser doesn't implement this method.
     * @deprecated Implement {@link StreamSchematicParser} or {@link FileSchematicParser} instead.
     */
    @Deprecated
    default Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException {
        throw new UnsupportedOperationException("This parser doesn't support legacy parsing.");
    }

}
