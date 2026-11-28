package com.bgsoftware.superiorskyblock.world.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.FileSchematicParser;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.world.schematic.impl.WorldEditSchematic7;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;

import java.io.File;
import java.io.FileInputStream;

public class FAWESchematicParser7 implements FileSchematicParser {

    @Override
    public Schematic parseSchematic(File file, String schematicName) throws SchematicParseException {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null)
            throw new SchematicParseException(schematicName + " is not a valid WorldEdit schematic.");

        try (FileInputStream fileInputStream = new FileInputStream(file);
             ClipboardReader reader = format.getReader(fileInputStream)) {
            Clipboard clipboard = reader.read();
            return new WorldEditSchematic7(schematicName, clipboard);
        } catch (Throwable error) {
            SchematicParseException parseException = new SchematicParseException(schematicName +
                    " could not be read as a WorldEdit schematic: " + error.getMessage());
            parseException.initCause(error);
            throw parseException;
        }
    }

}
