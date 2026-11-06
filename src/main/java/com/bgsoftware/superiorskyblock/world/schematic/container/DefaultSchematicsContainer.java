package com.bgsoftware.superiorskyblock.world.schematic.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.api.world.Dimension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultSchematicsContainer implements SchematicsContainer {

    private final Map<String, Schematic> schematicMap = new LinkedHashMap<>();
    private final List<String> defaultSchematics = new LinkedList<>();
    private final List<SchematicParser> schematicParsers = new LinkedList<>();

    @Nullable
    @Override
    public Schematic getSchematic(String name) {
        return this.schematicMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void addSchematic(Schematic schematic) {
        String schematicName = schematic.getName().toLowerCase(Locale.ENGLISH);

        this.schematicMap.put(schematicName, schematic);

        if (isDefaultSchematicInternal(schematicName)) {
            this.defaultSchematics.add(schematicName);
        }
    }

    @Override
    public Map<String, Schematic> getSchematics() {
        return Collections.unmodifiableMap(this.schematicMap);
    }

    @Override
    public boolean isDefaultSchematic(String schematic) {
        return this.defaultSchematics.contains(schematic.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void addSchematicParser(SchematicParser schematicParser) {
        this.schematicParsers.add(schematicParser);
    }

    @Override
    public List<SchematicParser> getSchematicParsers() {
        return Collections.unmodifiableList(this.schematicParsers);
    }

    @Override
    public void clearSchematics() {
        this.schematicMap.clear();
        this.defaultSchematics.clear();
    }

    private static boolean isDefaultSchematicInternal(String schematicName) {
        for (Dimension dimension : Dimension.values()) {
            String dimensionSuffix = "_" + dimension.getName().toLowerCase(Locale.ENGLISH);

            if (schematicName.endsWith(dimensionSuffix)) {
                return false;
            }
        }

        return true;
    }

}
