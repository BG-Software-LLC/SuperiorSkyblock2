package com.bgsoftware.superiorskyblock.world.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.core.io.IOUtils;
import com.bgsoftware.superiorskyblock.world.schematic.impl.WorldEditSchematic7;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;

public class FAWESchematicParser7 implements SchematicParser {

    public FAWESchematicParser7() {

    }

    @Override
    public Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException {
            File pluginFolder = com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin.getPlugin().getDataFolder();
            File schemFile = new File(pluginFolder, "schematics/" + schematicName + ".schem");
            File schematicFile = new File(pluginFolder, "schematics/" + schematicName + ".schematic");
            
            File targetFile = null;
            if (schemFile.exists()) {
                targetFile = schemFile;
            } else if (schematicFile.exists()) {
                targetFile = schematicFile;
            }

            if (targetFile != null) {
                ClipboardFormat format = ClipboardFormats.findByFile(targetFile);
                if (format != null) {
                    try (FileInputStream fis = new FileInputStream(targetFile);
                         ClipboardReader reader = format.getReader(fis)) {
                        Clipboard clipboard = reader.read();
                        return new WorldEditSchematic7(schematicName, clipboard);
                    } catch (Throwable error) {
                        error.printStackTrace();
                    }
                }
            }
            
            throw new SchematicParseException(schematicName + " is not a valid WorldEdit schematic.");
    }

}
