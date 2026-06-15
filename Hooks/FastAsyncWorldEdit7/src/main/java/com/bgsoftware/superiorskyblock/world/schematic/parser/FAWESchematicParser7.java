package com.bgsoftware.superiorskyblock.world.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.core.io.IOUtils;
import com.bgsoftware.superiorskyblock.world.schematic.impl.WorldEditSchematic;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class FAWESchematicParser7 implements SchematicParser {

    public FAWESchematicParser7() {

    }

    @Override
    public Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException {
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(bytes);
            }
            byte[] gzippedBytes = baos.toByteArray();

            ClipboardFormat spongeFormat = ClipboardFormats.findByAlias("schem");
            if (spongeFormat != null) {
                try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(gzippedBytes));
                     ClipboardReader reader = spongeFormat.getReader(stream)) {
                    Clipboard clipboard = reader.read();
                    return new WorldEditSchematic(schematicName, clipboard);
                } catch (Throwable ignored) {
                }
            }

            ClipboardFormat legacyFormat = ClipboardFormats.findByAlias("schematic");
            if (legacyFormat != null) {
                try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(gzippedBytes));
                     ClipboardReader reader = legacyFormat.getReader(stream)) {
                    Clipboard clipboard = reader.read();
                    return new WorldEditSchematic(schematicName, clipboard);
                } catch (Throwable ignored) {
                }
            }
            
            throw new SchematicParseException(schematicName + " is not a valid WorldEdit schematic.");
        } catch (Throwable error) {
            throw new SchematicParseException(schematicName + " is not a valid WorldEdit schematic.");
        }
    }

}
