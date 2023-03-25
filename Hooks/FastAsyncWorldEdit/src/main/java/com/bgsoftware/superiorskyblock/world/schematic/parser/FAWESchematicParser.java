package com.bgsoftware.superiorskyblock.world.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.world.schematic.impl.WorldEditSchematic;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;

import java.io.DataInputStream;
import java.io.IOException;

public class FAWESchematicParser implements SchematicParser {

    public FAWESchematicParser() {

    }

    @Override
    public Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException {
        try {
            //noinspection deprecation
            return new WorldEditSchematic(schematicName, ClipboardFormat.SCHEMATIC.load(inputStream));
        } catch (IOException error) {
            throw new SchematicParseException(schematicName + " is not a FastAsyncWorldEdit schematic.");
        }
    }

}
