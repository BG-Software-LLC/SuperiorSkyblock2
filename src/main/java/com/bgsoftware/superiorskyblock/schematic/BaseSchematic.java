package com.bgsoftware.superiorskyblock.schematic;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;

import java.util.Set;

@SuppressWarnings("WeakerAccess")
public abstract class BaseSchematic implements Schematic {

    protected final String name;

    protected final KeyMap<Integer> cachedCounts = new KeyMap<>();

    protected BaseSchematic(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract Set<ChunkPosition> getLoadedChunks();

}
