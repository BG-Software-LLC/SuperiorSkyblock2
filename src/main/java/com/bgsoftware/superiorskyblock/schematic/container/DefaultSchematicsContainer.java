package com.bgsoftware.superiorskyblock.schematic.container;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DefaultSchematicsContainer implements SchematicsContainer {

    private final Map<String, Schematic> schematicMap = new HashMap<>();
    private final List<SchematicParser> schematicParsers = new ArrayList<>();

    @Nullable
    @Override
    public Schematic getSchematic(String name) {
        return this.schematicMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void addSchematic(Schematic schematic) {
        this.schematicMap.put(schematic.getName().toLowerCase(Locale.ENGLISH), schematic);
    }

    @Override
    public List<String> getSchematicNames() {
        return Collections.unmodifiableList(new ArrayList<>(this.schematicMap.keySet()));
    }

    @Override
    public void addSchematicParser(SchematicParser schematicParser) {
        this.schematicParsers.add(schematicParser);
    }

    @Override
    public List<SchematicParser> getSchematicParsers() {
        return Collections.unmodifiableList(this.schematicParsers);
    }

}
