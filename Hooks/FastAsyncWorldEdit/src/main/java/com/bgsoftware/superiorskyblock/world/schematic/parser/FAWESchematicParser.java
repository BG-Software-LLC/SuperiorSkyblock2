package com.bgsoftware.superiorskyblock.world.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.core.io.IOUtils;
import com.bgsoftware.superiorskyblock.world.schematic.impl.WorldEditSchematic;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class FAWESchematicParser implements SchematicParser {

    public FAWESchematicParser() {

    }

    @Override
    public Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException {
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);

            // Try Sponge format first (for .schem files)
            ClipboardFormat spongeFormat = ClipboardFormats.findByAlias("sponge");
            if (spongeFormat != null) {
                try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes))) {
                    //noinspection deprecation
                    return new WorldEditSchematic(schematicName, spongeFormat.load(stream));
                } catch (Exception ignored) {
                }
            }

            // Fallback to legacy SCHEMATIC format
            try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes))) {
                //noinspection deprecation
                return new WorldEditSchematic(schematicName, ClipboardFormat.SCHEMATIC.load(stream));
            }
        } catch (IOException error) {
            throw new SchematicParseException(schematicName + " is not a FastAsyncWorldEdit schematic.");
        }
    }

}
