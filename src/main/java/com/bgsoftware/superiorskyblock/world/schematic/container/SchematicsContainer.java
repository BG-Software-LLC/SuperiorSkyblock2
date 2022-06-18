package com.bgsoftware.superiorskyblock.world.schematic.container;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;

import javax.annotation.Nullable;
import java.util.List;

public interface SchematicsContainer {

    @Nullable
    Schematic getSchematic(String name);

    void addSchematic(Schematic schematic);

    List<String> getSchematicNames();

    void addSchematicParser(SchematicParser schematicParser);

    List<SchematicParser> getSchematicParsers();

}
