package com.bgsoftware.superiorskyblock.world.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.schematic.impl.SuperiorSchematic;

import java.io.DataInputStream;
import java.io.IOException;

public class DefaultSchematicParser implements SchematicParser {

    private static final DefaultSchematicParser INSTANCE = new DefaultSchematicParser();

    public static DefaultSchematicParser getInstance() {
        return INSTANCE;
    }

    private DefaultSchematicParser() {

    }

    @Override
    public Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException {
        try {
            CompoundTag compoundTag = (CompoundTag) Tag.fromStream(inputStream, 0);

            if (ServerVersion.isLegacy() && compoundTag.containsKey("version") &&
                    !ServerVersion.getBukkitVersion().equals(compoundTag.getString("version")))
                Log.warn("Schematic ", schematicName, " was created in a different version, may cause issues.");

            if (!compoundTag.isEmpty())
                return new SuperiorSchematic(schematicName, compoundTag);
        } catch (IOException ignored) {
        }

        throw new SchematicParseException("This schematic is not valid.");
    }

}
