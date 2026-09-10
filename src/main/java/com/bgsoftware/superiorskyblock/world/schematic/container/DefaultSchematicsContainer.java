package com.bgsoftware.superiorskyblock.world.schematic.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultSchematicsContainer implements SchematicsContainer {

    private volatile Map<String, Schematic> schematicMap = Collections.emptyMap();
    private volatile List<SchematicParser> schematicParsers = Collections.emptyList();

    @Nullable
    @Override
    public Schematic getSchematic(String name) {
        return this.schematicMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void addSchematic(Schematic schematic) {
        String schematicName = schematic.getName().toLowerCase(Locale.ENGLISH);
        synchronized (this) {
            Map<String, Schematic> schematics = new LinkedHashMap<>(this.schematicMap);
            schematics.put(schematicName, schematic);
            this.schematicMap = Collections.unmodifiableMap(schematics);
        }
    }

    @Override
    public Map<String, Schematic> getSchematics() {
        return this.schematicMap;
    }

    @Override
    public synchronized void addSchematicParser(SchematicParser schematicParser) {
        List<SchematicParser> parsers = new LinkedList<>(this.schematicParsers);
        parsers.add(schematicParser);
        this.schematicParsers = Collections.unmodifiableList(parsers);
    }

    @Override
    public List<SchematicParser> getSchematicParsers() {
        return this.schematicParsers;
    }

    @Override
    public synchronized void clearSchematics() {
        this.schematicMap = Collections.emptyMap();
    }

    @Override
    public void replaceSchematics(Collection<Schematic> schematics) {
        Map<String, Schematic> newSchematics = new LinkedHashMap<>();
        for (Schematic schematic : schematics)
            newSchematics.put(schematic.getName().toLowerCase(Locale.ENGLISH), schematic);
        synchronized (this) {
            this.schematicMap = Collections.unmodifiableMap(newSchematics);
        }
    }

}
