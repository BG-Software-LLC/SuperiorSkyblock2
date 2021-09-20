package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.chunks.EmptyCounterChunkSection;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.utils.chunks.CalculatedChunk;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockDoubleStep;
import net.minecraft.server.v1_12_R1.BlockDoubleStepAbstract;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_12_R1.util.UnsafeList;
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

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();
        byte biomeBase = (byte) BiomeBase.REGISTRY_ID.a(CraftBlock.biomeToBiomeBase(biome));

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, null, chunk -> {
            Arrays.fill(chunk.getBiomeIndex(), biomeBase);
            chunk.markDirty();
        }, chunk -> {
            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunk.locX, chunk.locZ);
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

            removeEntities(chunk);

            for(Map.Entry<BlockPosition, TileEntity> tileEntityEntry : chunk.tileEntities.entrySet()) {
                worldServer.tileEntityListTick.remove(tileEntityEntry.getValue());
                worldServer.capturedTileEntities.remove(tileEntityEntry.getKey());
            }

            chunk.tileEntities.clear();

            removeBlocks(chunk);
        }, chunk -> {
            NMSUtils.sendPacketToRelevantPlayers(worldServer, chunk.locX, chunk.locZ, new PacketPlayOutMapChunk(chunk, 65535));
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
            ChunkPosition chunkPosition = ChunkPosition.of(worldServer.getWorld(), chunk.locX, chunk.locZ);

            KeyMap<Integer> blockCounts = new KeyMap<>();
            Set<Location> spawnersLocations = new HashSet<>();

            for (ChunkSection chunkSection : chunk.getSections()) {
                if (chunkSection != null && chunkSection != Chunk.a) {
                    for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        Block block = blockData.getBlock();
                        if (block != Blocks.AIR) {
                            Location location = new Location(worldServer.getWorld(),
                                    (chunkPosition.getX() << 4) + bp.getX(),
                                    chunkSection.getYPosition() + bp.getY(),
                                    (chunkPosition.getZ() << 4) + bp.getZ());
                            int blockAmount = 1;

                            if (block instanceof BlockDoubleStep) {
                                blockAmount = 2;
                                // Converts the block data to a regular slab
                                MinecraftKey blockKey = Block.REGISTRY.b(block);
                                blockData = Block.REGISTRY.get(new MinecraftKey(blockKey.getKey()
                                                .replace("double_", ""))).getBlockData()
                                        .set(BlockDoubleStepAbstract.VARIANT, blockData.get(BlockDoubleStepAbstract.VARIANT));
                                block = blockData.getBlock();
                            }

                            Material type = CraftMagicNumbers.getMaterial(block);
                            byte data = (byte) block.toLegacyData(blockData);
                            Key blockKey = Key.of(type, data, location);
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + blockAmount);
                            if (type == Material.MOB_SPAWNER) {
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

    @Override
    public void injectChunkSections(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        for (int i = 0; i < 16; i++)
            chunk.getSections()[i] = EmptyCounterChunkSection.of(chunk.getSections()[i]);
    }

    @Override
    public boolean isChunkEmpty(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        return Arrays.stream(chunk.getSections()).allMatch(chunkSection -> chunkSection == null || chunkSection.a());
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        NMSUtils.sendPacketToRelevantPlayers((WorldServer) chunk.world, chunk.locX, chunk.locZ,
                new PacketPlayOutMapChunk(chunk, 65535));
    }

    @Override
    public void refreshLights(org.bukkit.Chunk chunk, List<BlockData> blockDataList) {
        World world = ((CraftChunk) chunk).getHandle().getWorld();

        // Update lights for the blocks.
        for (BlockData blockData : blockDataList) {
            BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
            if (plugin.getSettings().isLightsUpdate() && blockData.getBlockLightLevel() > 0)
                world.a(EnumSkyBlock.BLOCK, blockPosition, blockData.getBlockLightLevel());

            byte skyLight = plugin.getSettings().isLightsUpdate() ? blockData.getSkyLightLevel() : 15;

            if (skyLight > 0 && blockData.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL)
                world.a(EnumSkyBlock.SKY, blockPosition, skyLight);
        }

        refreshChunk(chunk);
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().getChunkProviderServer()
                .getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (stop) {
            ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunk.getX(), chunk.getZ());
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.remove(chunkCoords);
            if (cropsTickingTileEntity != null)
                cropsTickingTileEntity.getWorld().tileEntityListTick.remove(cropsTickingTileEntity);
        } else {
            CropsTickingTileEntity.create(island, ((CraftChunk) chunk).getHandle());
        }
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

    private static void removeBlocks(Chunk chunk) {
        WorldServer worldServer = (WorldServer) chunk.world;

        if (worldServer.generator != null && !(worldServer.generator instanceof IslandsGenerator)) {
            CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(worldServer, 0L, worldServer.generator);
            Chunk generatedChunk = customChunkGenerator.getOrCreateChunk(chunk.locX, chunk.locZ);

            for (int i = 0; i < 16; i++)
                chunk.getSections()[i] = generatedChunk.getSections()[i];

            for (Map.Entry<BlockPosition, TileEntity> entry : generatedChunk.getTileEntities().entrySet())
                worldServer.setTileEntity(entry.getKey(), entry.getValue());
        }
    }

}
