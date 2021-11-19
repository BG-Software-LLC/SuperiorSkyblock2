package com.bgsoftware.superiorskyblock.api.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;

import java.io.DataInputStream;

public interface SchematicParser {

    Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException;

}
