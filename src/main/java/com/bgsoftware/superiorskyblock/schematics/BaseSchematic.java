package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import org.bukkit.Location;

import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
public abstract class BaseSchematic implements Schematic {

    protected final String name;

    protected final KeyMap<Integer> cachedCounts = new KeyMap<>();

    protected BaseSchematic(String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    protected static class PasteSchematicData {

        protected final Schematic schematic;
        protected final Island island;
        protected final Location location;
        protected final Runnable callback;
        protected final Consumer<Throwable> onFailure;

        protected PasteSchematicData(Schematic schematic, Island island, Location location, Runnable callback, Consumer<Throwable> onFailure) {
            this.schematic = schematic;
            this.island = island;
            this.location = location;
            this.callback = callback;
            this.onFailure = onFailure;
        }
    }

    public Location getTeleportLocation(Location location){
        return location;
    }

}
