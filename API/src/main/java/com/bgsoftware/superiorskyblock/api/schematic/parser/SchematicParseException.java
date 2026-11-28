package com.bgsoftware.superiorskyblock.api.schematic.parser;

/**
 * This exception is thrown by {@link SchematicParser}s when the given schematic
 * doesn't meet the format of the parser.
 */
public class SchematicParseException extends Exception {

    public SchematicParseException(String error) {
        super(error);
    }

}
