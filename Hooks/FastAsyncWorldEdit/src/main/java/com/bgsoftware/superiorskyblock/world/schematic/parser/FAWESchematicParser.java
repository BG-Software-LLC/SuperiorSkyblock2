package com.bgsoftware.superiorskyblock.world.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.StreamSchematicParser;
import com.bgsoftware.superiorskyblock.world.schematic.impl.WorldEditSchematic;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class FAWESchematicParser implements StreamSchematicParser {

    @Override
    public Schematic parseSchematic(InputStream inputStream, String schematicName) throws SchematicParseException {
        try {
            // Legacy FAWE loads MCEdit schematics from the GZIP-decompressed stream.
            DataInputStream decompressedStream = new DataInputStream(new GZIPInputStream(inputStream));
            //noinspection deprecation
            return new WorldEditSchematic(schematicName, ClipboardFormat.SCHEMATIC.load(decompressedStream));
        } catch (IOException error) {
            throw new SchematicParseException(schematicName + " is not a FastAsyncWorldEdit schematic.");
        }
    }

}
