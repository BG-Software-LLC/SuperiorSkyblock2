package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.collections.Chunk2ObjectMap;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.chunks.EmptyCounterChunkSection;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
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
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_12_R1.util.UnsafeList;
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

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        byte biomeBase = (byte) BiomeBase.REGISTRY_ID.a(CraftBlock.biomeToBiomeBase(biome));

        NMSUtils.runActionOnChunks(chunkPositions, true, new NMSUtils.ChunkCallback() {
            @Override
            public void onChunk(Chunk chunk, boolean isLoaded) {
                Arrays.fill(chunk.getBiomeIndex(), biomeBase);
                chunk.markDirty();
            }

            @Override
            public void onUpdateChunk(Chunk chunk) {
                PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunk.locX, chunk.locZ);
                PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

                playersToUpdate.forEach(player -> {
                    PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                    playerConnection.sendPacket(unloadChunkPacket);
                    playerConnection.sendPacket(mapChunkPacket);
                });
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
                Arrays.fill(chunk.getSections(), Chunk.a);

                removeEntities(chunk);

                for (Map.Entry<BlockPosition, TileEntity> tileEntityEntry : chunk.tileEntities.entrySet()) {
                    chunk.world.tileEntityListTick.remove(tileEntityEntry.getValue());
                    chunk.world.capturedTileEntities.remove(tileEntityEntry.getKey());
                }

                chunk.tileEntities.clear();

                removeBlocks(chunk);
            }

            @Override
            public void onUpdateChunk(Chunk chunk) {
                // Do nothing.
            }

            @Override
            public void onFinish() {
                if (onFinish != null)
                    onFinish.run();
            }
        });
    }

    private static boolean isChunkSectionEmpty(ChunkSection chunkSection) {
        return chunkSection instanceof EmptyCounterChunkSection && ((EmptyCounterChunkSection) chunkSection).isEmpty();
    }

    @Override
    public CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions,
                                                                    Synchronized<Chunk2ObjectMap<CalculatedChunk>> unloadedChunksCache) {
        List<CalculatedChunk> allCalculatedChunks = new LinkedList<>();
        List<ChunkPosition> chunkPositionsToCalculate = new LinkedList<>();

        Iterator<ChunkPosition> chunkPositionsIterator = chunkPositions.iterator();
        while (chunkPositionsIterator.hasNext()) {
            ChunkPosition chunkPosition = chunkPositionsIterator.next();
            CalculatedChunk cachedCalculatedChunk = unloadedChunksCache.readAndGet(m -> m.get(chunkPosition));
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

        NMSUtils.runActionOnChunks(chunkPositionsToCalculate, false, new NMSUtils.ChunkCallback() {
            @Override
            public void onChunk(Chunk chunk, boolean isLoaded) {
                World bukkitWorld = chunk.world.getWorld();
                ChunkPosition chunkPosition = ChunkPosition.of(bukkitWorld, chunk.locX, chunk.locZ);

                KeyMap<Counter> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
                List<Location> spawnersLocations = new LinkedList<>();

                for (ChunkSection chunkSection : chunk.getSections()) {
                    if (chunkSection != null && chunkSection != Chunk.a && !isChunkSectionEmpty(chunkSection)) {
                        for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
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
                                    MinecraftKey blockKey = Block.REGISTRY.b(block);
                                    blockData = Block.REGISTRY.get(new MinecraftKey(blockKey.getKey()
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
                    unloadedChunksCache.write(m -> m.put(chunkPosition, calculatedChunk));
            }

            @Override
            public void onUpdateChunk(Chunk chunk) {
                // Do nothing.
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
            public void onUpdateChunk(Chunk chunk) {
                // Do nothing
            }

            @Override
            public void onFinish() {
                completableFuture.complete(chunkEntities);
            }
        });

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
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().getChunkProviderServer()
                .getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public void updateCropsTicker(List<ChunkPosition> chunkPositions, double newCropGrowthMultiplier) {
        if (chunkPositions.isEmpty()) return;
        CropsTickingTileEntity.forEachChunk(chunkPositions, cropsTickingTileEntity ->
                cropsTickingTileEntity.setCropGrowthMultiplier(newCropGrowthMultiplier));
    }

    @Override
    public void shutdown() {
        List<CompletableFuture<Void>> pendingTasks = NMSUtils.getPendingChunkActions();

        if (pendingTasks.isEmpty())
            return;

        Log.info("Waiting for chunk tasks to complete.");

        CompletableFuture.allOf(pendingTasks.toArray(new CompletableFuture[0])).join();
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

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;

        if (stop) {
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.remove(
                    ChunkCoordIntPair.a(chunk.getX(), chunk.getZ()));
            if (cropsTickingTileEntity != null)
                cropsTickingTileEntity.getWorld().tileEntityListTick.remove(cropsTickingTileEntity);
        } else {
            CropsTickingTileEntity.create(island, ((CraftChunk) chunk).getHandle());
        }
    }

}
