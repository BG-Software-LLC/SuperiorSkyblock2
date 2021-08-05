package com.bgsoftware.superiorskyblock.nms.v1_13_R1;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.generator.WorldGenerator;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.objects.CalculatedChunk;
import net.minecraft.server.v1_13_R1.BiomeBase;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.BlockProperties;
import net.minecraft.server.v1_13_R1.BlockPropertySlabType;
import net.minecraft.server.v1_13_R1.Blocks;
import net.minecraft.server.v1_13_R1.Chunk;
import net.minecraft.server.v1_13_R1.ChunkConverter;
import net.minecraft.server.v1_13_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_13_R1.ChunkSection;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.IBlockData;
import net.minecraft.server.v1_13_R1.IChunkAccess;
import net.minecraft.server.v1_13_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_13_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_13_R1.PlayerConnection;
import net.minecraft.server.v1_13_R1.ProtoChunk;
import net.minecraft.server.v1_13_R1.TagsBlock;
import net.minecraft.server.v1_13_R1.TileEntity;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_13_R1.util.UnsafeList;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, null, chunk -> {
            BiomeBase[] biomeIndex = chunk.getBiomeIndex();

            if (chunk instanceof ProtoChunk && biomeIndex == null) {
                biomeIndex = new BiomeBase[256];
                chunk.a(biomeIndex);
            }

            Arrays.fill(biomeIndex, biomeBase);
            if (chunk instanceof Chunk)
                ((Chunk) chunk).markDirty();
        }, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();
            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.x, chunkCoords.z);
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        });
    }

    @Override
    public void deleteChunks(Island island, List<ChunkPosition> chunkPositions, Runnable onFinish) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

        chunkPositions.forEach(chunkPosition -> ChunksTracker.markEmpty(island, chunkPosition, false));

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, onFinish, chunk -> {
            Arrays.fill(chunk.getSections(), Chunk.a);

            if (chunk instanceof Chunk) {
                removeEntities((Chunk) chunk);
                new HashSet<>(((Chunk) chunk).tileEntities.keySet()).forEach(((Chunk) chunk).world::n);
                ((Chunk) chunk).tileEntities.clear();
            } else {
                ((ProtoChunk) chunk).r().clear();
                ((ProtoChunk) chunk).s().clear();
            }

            removeBlocks(worldServer, chunk);
        }, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();
            NMSUtils.sendPacketToRelevantPlayers(worldServer, chunkCoords.x, chunkCoords.z, new PacketPlayOutMapChunk(chunk, 65535));
        });
    }

    @Override
    public CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions) {
        CompletableFuture<List<CalculatedChunk>> completableFuture = new CompletableFuture<>();
        List<CalculatedChunk> allCalculatedChunks = new ArrayList<>();

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());
        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, false, () -> {
            completableFuture.complete(allCalculatedChunks);
        }, chunk -> {
            ChunkPosition chunkPosition = ChunkPosition.of(worldServer.getWorld(), chunk.getPos().x, chunk.getPos().z);

            KeyMap<Integer> blockCounts = new KeyMap<>();
            Set<Location> spawnersLocations = new HashSet<>();

            for (ChunkSection chunkSection : chunk.getSections()) {
                if (chunkSection != null && chunkSection != Chunk.a) {
                    for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        if (blockData.getBlock() != Blocks.AIR) {
                            Location location = new Location(worldServer.getWorld(),
                                    (chunkPosition.getX() << 4) + bp.getX(),
                                    chunkSection.getYPosition() + bp.getY(),
                                    (chunkPosition.getZ() << 4) + bp.getZ());
                            int blockAmount = 1;

                            if ((blockData.getBlock().a(TagsBlock.x) || blockData.getBlock().a(TagsBlock.i)) &&
                                    blockData.get(BlockProperties.at) == BlockPropertySlabType.DOUBLE) {
                                blockAmount = 2;
                                blockData = blockData.set(BlockProperties.at, BlockPropertySlabType.BOTTOM);
                            }

                            Material type = CraftMagicNumbers.getMaterial(blockData.getBlock());
                            Key blockKey = Key.of(type.name() + "", "", location);
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + blockAmount);
                            if (type == Material.SPAWNER) {
                                spawnersLocations.add(location);
                            }
                        }
                    }
                }
            }

            allCalculatedChunks.add(new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations));
        }, null);

        return completableFuture;
    }

    private static void removeEntities(Chunk chunk) {
        for (int i = 0; i < chunk.entitySlices.length; i++) {
            chunk.entitySlices[i].forEach(entity -> {
                if (!(entity instanceof EntityHuman))
                    entity.dead = true;
            });
            chunk.entitySlices[i] = new UnsafeList<>();
        }
    }

    private static void removeBlocks(WorldServer worldServer, IChunkAccess chunkAccess) {
        ChunkCoordIntPair chunkCoords = chunkAccess.getPos();

        if (worldServer.generator != null && !(worldServer.generator instanceof WorldGenerator)) {
            CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(worldServer, 0L, worldServer.generator);
            ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);
            customChunkGenerator.createChunk(protoChunk);

            for (int i = 0; i < 16; i++)
                chunkAccess.getSections()[i] = protoChunk.getSections()[i];

            for (Map.Entry<BlockPosition, TileEntity> entry : protoChunk.r().entrySet())
                worldServer.setTileEntity(entry.getKey(), entry.getValue());
        }
    }

}
