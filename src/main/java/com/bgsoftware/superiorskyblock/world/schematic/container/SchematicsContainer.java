package com.bgsoftware.superiorskyblock.world.schematic.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;

import java.util.List;
import java.util.Map;

public interface SchematicsContainer {

    @Nullable
    Schematic getSchematic(String name);

    void addSchematic(Schematic schematic);

    Map<String, Schematic> getSchematics();

    void addSchematicParser(SchematicParser schematicParser);

    List<SchematicParser> getSchematicParsers();

    void clearSchematics();

}
