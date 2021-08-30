package com.bgsoftware.superiorskyblock.schematic.container;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;

import javax.annotation.Nullable;
import java.util.List;

public interface SchematicsContainer {

    @Nullable
    Schematic getSchematic(String name);

    void addSchematic(Schematic schematic);

    List<String> getSchematicNames();

}
