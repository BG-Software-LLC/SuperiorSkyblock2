package com.bgsoftware.superiorskyblock.nms.v1_16_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import net.minecraft.server.v1_16_R1.BiomeBase;
import net.minecraft.server.v1_16_R1.BiomeStorage;
import net.minecraft.server.v1_16_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R1.IRegistry;
import net.minecraft.server.v1_16_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_16_R1.PlayerConnection;
import net.minecraft.server.v1_16_R1.WorldServer;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class NMSChunksImpl implements NMSChunks {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(BiomeStorage.class, BiomeBase[].class, "g");

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, null, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();
            BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(chunk.getBiomeIndex());

            if (biomeBases == null)
                throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkCoords.x + "," + chunkCoords.z + ").");

            Arrays.fill(biomeBases, biomeBase);
            chunk.markDirty();

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.x, chunkCoords.z);
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535, true);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        }, unloadedChunk -> {
            int[] biomes = unloadedChunk.hasKeyOfType("Biomes", 11) ? unloadedChunk.getIntArray("Biomes") : new int[256];
            Arrays.fill(biomes, IRegistry.BIOME.a(biomeBase));
            unloadedChunk.setIntArray("Biomes", biomes);
        });
    }

}
