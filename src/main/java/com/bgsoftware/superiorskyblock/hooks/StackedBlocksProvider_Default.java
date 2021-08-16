package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import org.bukkit.World;

import java.util.Collection;
import java.util.stream.Collectors;

public final class StackedBlocksProvider_Default implements StackedBlocksProvider_AutoDetect {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public Collection<Pair<Key, Integer>> getBlocks(World world, int chunkX, int chunkZ) {
        return plugin.getGrid().getStackedBlocks(ChunkPosition.of(world, chunkX, chunkZ)).stream()
                .map(stackedBlock -> new Pair<>((Key) stackedBlock.getBlockKey(), stackedBlock.getAmount()))
                .collect(Collectors.toSet());
    }

}
