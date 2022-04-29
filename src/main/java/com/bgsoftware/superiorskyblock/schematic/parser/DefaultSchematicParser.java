package com.bgsoftware.superiorskyblock.schematic.parser;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.schematic.impl.SuperiorSchematic;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;

import java.io.DataInputStream;
import java.io.IOException;

public final class DefaultSchematicParser implements SchematicParser {

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

            if (ServerVersion.isLegacy() && compoundTag.getValue().containsKey("version") &&
                    !compoundTag.getValue().get("version").getValue().equals(ServerVersion.getBukkitVersion()))
                SuperiorSkyblockPlugin.log("&cSchematic " + schematicName + " was created in a different version, may cause issues.");

            if (!compoundTag.getValue().isEmpty())
                return new SuperiorSchematic(schematicName, compoundTag);
        } catch (IOException ignored) {
        }

        throw new SchematicParseException("This schematic is not valid.");
    }

}
