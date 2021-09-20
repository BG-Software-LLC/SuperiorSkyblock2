package com.bgsoftware.superiorskyblock.schematic.container;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultSchematicsContainer implements SchematicsContainer {

    private final Map<String, Schematic> schematicMap = new HashMap<>();

    @Nullable
    @Override
    public Schematic getSchematic(String name) {
        return this.schematicMap.get(name.toLowerCase());
    }

    @Override
    public void addSchematic(Schematic schematic) {
        this.schematicMap.put(schematic.getName().toLowerCase(), schematic);
    }

    @Override
    public List<String> getSchematicNames() {
        return Collections.unmodifiableList(new ArrayList<>(this.schematicMap.keySet()));
    }

}
