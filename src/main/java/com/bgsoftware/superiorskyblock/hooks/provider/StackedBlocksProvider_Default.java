package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.World;

import java.util.Collection;
import java.util.Collections;

public final class StackedBlocksProvider_Default implements StackedBlocksProvider_AutoDetect {

    @Override
    public Collection<Pair<Key, Integer>> getBlocks(World world, int chunkX, int chunkZ) {
        return Collections.emptyList();
    }

}
