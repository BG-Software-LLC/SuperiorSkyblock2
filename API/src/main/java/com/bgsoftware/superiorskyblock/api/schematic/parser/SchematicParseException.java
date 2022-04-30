package com.bgsoftware.superiorskyblock.api.schematic.parser;

import java.io.DataInputStream;

/**
 * This exception is used inside {@link SchematicParser#parseSchematic(DataInputStream, String)}
 * when a faulty stream is given to the parser.
 */
public class SchematicParseException extends Exception {

    public SchematicParseException(String error) {
        super(error);
    }

}
