package com.bgsoftware.superiorskyblock.nms.v1_8_R2;

import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import net.minecraft.server.v1_8_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R2.WorldServer;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.block.CraftBlock;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class NMSChunksImpl implements NMSChunks {

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();
        byte biomeBase = (byte) CraftBlock.biomeToBiomeBase(biome).id;

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, null, chunk -> {
            Arrays.fill(chunk.getBiomeIndex(), biomeBase);
            chunk.e();
        }, null);
    }

}
