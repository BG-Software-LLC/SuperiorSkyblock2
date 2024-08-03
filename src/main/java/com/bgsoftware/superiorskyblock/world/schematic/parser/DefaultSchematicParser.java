package com.bgsoftware.superiorskyblock.world.schematic.parser;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.io.IOUtils;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.schematic.impl.SuperiorSchematic;
import com.google.common.hash.Hashing;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DefaultSchematicParser implements SchematicParser {

    private static final DefaultSchematicParser INSTANCE = new DefaultSchematicParser();

    private static final Map<String, SuperiorSchematic> HASHED_SCHEMATIC = new HashMap<>();

    public static DefaultSchematicParser getInstance() {
        return INSTANCE;
    }

    private DefaultSchematicParser() {

    }

    @Override
    public Schematic parseSchematic(DataInputStream inputStream, String schematicName) throws SchematicParseException {
        try {
            byte[] schematicData = IOUtils.toByteArray(inputStream);
            String hashedData = Hashing.sha256().hashBytes(schematicData).toString();
            SuperiorSchematic hashedSchematic = HASHED_SCHEMATIC.get(hashedData);
            if (hashedSchematic != null)
                return hashedSchematic.copy(schematicName);

            DataInputStream schematicStream = new DataInputStream(new ByteArrayInputStream(schematicData));
            CompoundTag compoundTag = (CompoundTag) Tag.fromStream(schematicStream, 0);

            if (ServerVersion.isLegacy() && compoundTag.containsKey("version") &&
                    !ServerVersion.getBukkitVersion().equals(compoundTag.getString("version")))
                Log.warn("Schematic ", schematicName, " was created in a different version, may cause issues.");

            if (!compoundTag.isEmpty()) {
                SuperiorSchematic schematic = new SuperiorSchematic(schematicName, compoundTag);
                HASHED_SCHEMATIC.put(hashedData, schematic);
                return schematic;
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }

        throw new SchematicParseException("This schematic is not valid.");
    }

}
