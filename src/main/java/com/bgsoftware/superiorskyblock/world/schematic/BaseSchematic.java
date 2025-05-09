package com.bgsoftware.superiorskyblock.world.schematic;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class BaseSchematic implements Schematic {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final String name;

    protected final KeyMap<Integer> cachedCounts;

    protected BaseSchematic(String name) {
        this(name, KeyMaps.createArrayMap(KeyIndicator.MATERIAL));
    }

    protected BaseSchematic(String name, KeyMap<Integer> cachedCounts) {
        this.name = name;
        this.cachedCounts = cachedCounts;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<Key, Integer> getBlockCounts() {
        return Collections.unmodifiableMap(cachedCounts);
    }

    public abstract List<ChunkPosition> getAffectedChunks();

    @Nullable
    public abstract Runnable onTeleportCallback();

}
