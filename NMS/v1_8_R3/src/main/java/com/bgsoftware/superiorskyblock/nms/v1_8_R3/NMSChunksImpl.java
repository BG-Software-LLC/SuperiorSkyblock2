package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockDoubleStep;
import net.minecraft.server.v1_8_R3.BlockDoubleStepAbstract;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_8_R3.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NMSChunksImpl implements NMSChunks {

    private final SuperiorSkyblockPlugin plugin;

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        KeyBlocksCache.cacheAllBlocks();
    }

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        byte biomeBase = (byte) CraftBlock.biomeToBiomeBase(biome).id;

        NMSUtils.runActionOnChunks(chunkPositions, true, new NMSUtils.ChunkCallback() {
            @Override
            public void onChunk(Chunk chunk, boolean isLoaded) {
                Arrays.fill(chunk.getBiomeIndex(), biomeBase);
                chunk.e();
            }

            @Override
            public void onFinish() {
                // Do nothing.
            }
        });
    }

    @Override
    public void deleteChunks(Island island, List<ChunkPosition> chunkPositions, @Nullable Runnable onFinish) {
        if (chunkPositions.isEmpty())
            return;

        chunkPositions.forEach(chunkPosition -> island.markChunkEmpty(chunkPosition.getWorld(),
                chunkPosition.getX(), chunkPosition.getZ(), false));

        NMSUtils.runActionOnChunks(chunkPositions, true, new NMSUtils.ChunkCallback() {
            @Override
            public void onChunk(Chunk chunk, boolean isLoaded) {
                Arrays.fill(chunk.getSections(), null);

                removeEntities(chunk);

                for (Map.Entry<BlockPosition, TileEntity> tileEntityEntry : chunk.tileEntities.entrySet()) {
                    chunk.world.tileEntityList.remove(tileEntityEntry.getValue());
                    try {
                        // This field doesn't exist in Taco 1.8
                        chunk.world.h.remove(tileEntityEntry.getValue());
                    } catch (Throwable ignored) {
                    }
                    chunk.world.capturedTileEntities.remove(tileEntityEntry.getKey());
                }

                chunk.tileEntities.clear();

                removeBlocks(chunk);
            }

            @Override
            public void onFinish() {
                if (onFinish != null)
                    onFinish.run();
            }
        });
    }

    @Override
    public CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions,
                                                                    Map<ChunkPosition, CalculatedChunk> unloadedChunksCache) {
        List<CalculatedChunk> allCalculatedChunks = new LinkedList<>();
        List<ChunkPosition> chunkPositionsToCalculate = new LinkedList<>();

        Iterator<ChunkPosition> chunkPositionsIterator = chunkPositions.iterator();
        while (chunkPositionsIterator.hasNext()) {
            ChunkPosition chunkPosition = chunkPositionsIterator.next();
            CalculatedChunk cachedCalculatedChunk = unloadedChunksCache.get(chunkPosition);
            if (cachedCalculatedChunk != null) {
                allCalculatedChunks.add(cachedCalculatedChunk);
                chunkPositionsIterator.remove();
            } else {
                chunkPositionsToCalculate.add(chunkPosition);
            }
        }

        if (chunkPositions.isEmpty())
            return CompletableFuture.completedFuture(allCalculatedChunks);

        CompletableFuture<List<CalculatedChunk>> completableFuture = new CompletableFuture<>();

        NMSUtils.runActionOnChunks(chunkPositions, false, new NMSUtils.ChunkCallback() {
            @Override
            public void onChunk(Chunk chunk, boolean isLoaded) {
                World bukkitWorld = chunk.world.getWorld();
                ChunkPosition chunkPosition = ChunkPosition.of(bukkitWorld, chunk.locX, chunk.locZ);

                KeyMap<Counter> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
                List<Location> spawnersLocations = new LinkedList<>();

                for (ChunkSection chunkSection : chunk.getSections()) {
                    if (chunkSection != null && !chunkSection.a()) {
                        for (BlockPosition bp : BlockPosition.b(new BlockPosition(0, 0, 0), new BlockPosition(15, 15, 15))) {
                            IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                            Block block = blockData.getBlock();
                            if (block != Blocks.AIR) {
                                Location location = new Location(bukkitWorld,
                                        (chunkPosition.getX() << 4) + bp.getX(),
                                        chunkSection.getYPosition() + bp.getY(),
                                        (chunkPosition.getZ() << 4) + bp.getZ());
                                int blockAmount = 1;

                                if (block instanceof BlockDoubleStep) {
                                    blockAmount = 2;
                                    // Converts the block data to a regular slab
                                    MinecraftKey blockKey = Block.REGISTRY.c(block);
                                    blockData = Block.REGISTRY.get(new MinecraftKey(blockKey.a()
                                                    .replace("double_", ""))).getBlockData()
                                            .set(BlockDoubleStepAbstract.VARIANT, blockData.get(BlockDoubleStepAbstract.VARIANT));
                                }

                                Key blockKey = Keys.of(KeyBlocksCache.getBlockKey(blockData), location);
                                blockCounts.computeIfAbsent(blockKey, b -> new Counter(0)).inc(blockAmount);
                                if (block == Blocks.MOB_SPAWNER) {
                                    spawnersLocations.add(location);
                                }
                            }
                        }
                    }
                }

                CalculatedChunk calculatedChunk = new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations);
                allCalculatedChunks.add(calculatedChunk);

                if (!isLoaded)
                    unloadedChunksCache.put(chunkPosition, calculatedChunk);
            }

            @Override
            public void onFinish() {
                completableFuture.complete(allCalculatedChunks);
            }
        });

        return completableFuture;
    }

    @Override
    public CompletableFuture<KeyMap<Counter>> calculateChunkEntities(Collection<ChunkPosition> chunkPositions) {
        if (chunkPositions.isEmpty())
            return CompletableFuture.completedFuture(KeyMaps.createEmptyMap());

        CompletableFuture<KeyMap<Counter>> completableFuture = new CompletableFuture<>();

        KeyMap<Counter> chunkEntities = KeyMaps.createArrayMap(KeyIndicator.ENTITY_TYPE);

        NMSUtils.runActionOnChunks(chunkPositions, false, new NMSUtils.ChunkCallback() {

            @Override
            public void onChunk(Chunk chunk, boolean isLoaded) {
                for (org.bukkit.entity.Entity bukkitEntity : chunk.bukkitChunk.getEntities()) {
                    if (!BukkitEntities.canBypassEntityLimit(bukkitEntity))
                        chunkEntities.computeIfAbsent(Keys.of(bukkitEntity), i -> new Counter(0)).inc(1);
                }
            }

            @Override
            public void onFinish() {
                completableFuture.complete(chunkEntities);
            }
        });

        return completableFuture;
    }

    @Override
    public void injectChunkSections(org.bukkit.Chunk chunk) {
        // No implementation
    }

    @Override
    public boolean isChunkEmpty(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        return Arrays.stream(chunk.getSections()).allMatch(chunkSection -> chunkSection == null || chunkSection.a());
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().chunkProviderServer
                .getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;

        if (stop) {
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.remove(
                    ChunkCoordIntPair.a(chunk.getX(), chunk.getZ()));
            if (cropsTickingTileEntity != null) {
                try {
                    cropsTickingTileEntity.getWorld().tileEntityList.remove(cropsTickingTileEntity);
                } catch (Throwable error) {
                    cropsTickingTileEntity.getWorld().t(cropsTickingTileEntity.getPosition());
                }
            }
        } else {
            CropsTickingTileEntity.create(island, ((CraftChunk) chunk).getHandle());
        }
    }

    @Override
    public void updateCropsTicker(List<ChunkPosition> chunkPositions, double newCropGrowthMultiplier) {
        if (chunkPositions.isEmpty()) return;
        CropsTickingTileEntity.forEachChunk(chunkPositions, cropsTickingTileEntity ->
                cropsTickingTileEntity.setCropGrowthMultiplier(newCropGrowthMultiplier));
    }

    @Override
    public void shutdown() {
        // Do nothing. There are no tasks to wait for.
    }

    @Override
    public List<Location> getBlockEntities(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        List<Location> blockEntities = new LinkedList<>();

        World bukkitWorld = bukkitChunk.getWorld();

        chunk.getTileEntities().keySet().forEach(blockPosition ->
                blockEntities.add(new Location(bukkitWorld, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ())));

        return blockEntities;
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
