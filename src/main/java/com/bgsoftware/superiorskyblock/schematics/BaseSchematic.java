package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.utils.queue.Queue;
import org.bukkit.Location;

@SuppressWarnings("WeakerAccess")
public abstract class BaseSchematic implements Schematic {

    protected Queue<PasteSchematicData> pasteSchematicQueue = new Queue<>();
    protected static boolean schematicProgress = false;

    protected static class PasteSchematicData {

        protected Schematic schematic;
        protected Island island;
        protected Location location;
        protected Runnable callback;

        protected PasteSchematicData(Schematic schematic, Island island, Location location, Runnable callback) {
            this.schematic = schematic;
            this.island = island;
            this.location = location;
            this.callback = callback;
        }
    }

    public Location getTeleportLocation(Location location){
        return location;
    }

}
