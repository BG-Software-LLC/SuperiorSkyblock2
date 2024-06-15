package com.bgsoftware.superiorskyblock.external.stackedblocks;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.World;

import java.util.Collection;

public class StackedBlocksProvider_Default implements StackedBlocksProvider_AutoDetect {

    @Override
    public Collection<Pair<Key, Integer>> getBlocks(World world, int chunkX, int chunkZ) {
        return Lists.emptyList();
    }

}
