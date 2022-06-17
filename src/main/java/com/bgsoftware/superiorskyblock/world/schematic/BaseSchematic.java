package com.bgsoftware.superiorskyblock.world.schematic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;

import java.util.Set;

@SuppressWarnings("WeakerAccess")
public abstract class BaseSchematic implements Schematic {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final String name;

    protected final KeyMap<Integer> cachedCounts = KeyMapImpl.createHashMap();

    protected BaseSchematic(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract Set<ChunkPosition> getLoadedChunks();

}
