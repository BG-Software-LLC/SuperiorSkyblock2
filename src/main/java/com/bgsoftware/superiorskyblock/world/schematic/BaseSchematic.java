package com.bgsoftware.superiorskyblock.world.schematic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;

import java.util.Collections;
import java.util.Map;
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

    @Override
    public Map<Key, Integer> getBlockCounts() {
        return Collections.unmodifiableMap(cachedCounts);
    }

    public abstract Set<ChunkPosition> getLoadedChunks();

}
