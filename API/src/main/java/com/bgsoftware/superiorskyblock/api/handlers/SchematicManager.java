package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;

import java.util.List;

@SuppressWarnings("unused")
public interface SchematicManager {

    Schematic getSchematic(String name);

    List<String> getSchematics();

    void saveSchematic(SuperiorPlayer superiorPlayer, String schematicName);

    void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, String schematicName);

}
