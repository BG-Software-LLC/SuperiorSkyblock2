package com.bgsoftware.superiorskyblock.nms.v1_12_R1.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.LongIterator;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.chunks.EmptyCounterChunkSection;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockBed;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WorldEditSessionImpl implements WorldEditSession {

    private static final ObjectsPool<WorldEditSessionImpl> POOL = new ObjectsPool<>(WorldEditSessionImpl::new);

    private static final ReflectMethod<Void> TILE_ENTITY_LOAD = new ReflectMethod<>(TileEntity.class, "a", NBTTagCompound.class);

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Long2ObjectMapView<ChunkData> chunks = CollectionsFactory.createLong2ObjectArrayMap();
    private final List<Pair<BlockPosition, IBlockData>> blocksToUpdate = new LinkedList<>();
    private final List<Pair<BlockPosition, CompoundTag>> blockEntities = new LinkedList<>();
    private final List<BlockPosition> lights = new LinkedList<>();
    private WorldServer worldServer;
    private Dimension dimension;

    private Location baseLocationForCache = null;

    public static WorldEditSessionImpl obtain(WorldServer worldServer) {
        return POOL.obtain().initialize(worldServer);
    }

    private WorldEditSessionImpl() {
    }

    public WorldEditSessionImpl initialize(WorldServer worldServer) {
        this.worldServer = worldServer;
        this.dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(worldServer.getWorld());
        return this;
    }

    @Override
    public void setBlock(Location location, int combinedId, @Nullable CompoundTag statesTag,
                         @Nullable CompoundTag blockEntityData) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (!isValidPosition(blockPosition))
            return;

        IBlockData blockData = Block.getByCombinedId(combinedId);

        if ((blockData.getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) ||
                blockData.getBlock() instanceof BlockBed) {
            blocksToUpdate.add(new Pair<>(blockPosition, blockData));
            return;
        }

        ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(blockPosition);

        if (blockEntityData != null)
            blockEntities.add(new Pair<>(blockPosition, blockEntityData));

        if (plugin.getSettings().isLightsUpdate() && blockData.d() > 0)
            lights.add(blockPosition);

        ChunkData chunkData = this.chunks.computeIfAbsent(ChunkCoordIntPair.a(chunkCoord.x, chunkCoord.z), ChunkData::new);

        ChunkSection chunkSection = chunkData.chunkSections[blockPosition.getY() >> 4];

        int blockX = blockPosition.getX() & 15;
        int blockY = blockPosition.getY();
        int blockZ = blockPosition.getZ() & 15;

        chunkSection.setType(blockX, blockY & 15, blockZ, blockData);
    }

    @Override
    public List<ChunkPosition> getAffectedChunks() {
        if (chunks.isEmpty())
            return Collections.emptyList();

        List<ChunkPosition> chunkPositions = new LinkedList<>();
        World bukkitWorld = worldServer.getWorld();
        LongIterator iterator = chunks.keyIterator();
        while (iterator.hasNext()) {
            long chunkKey = iterator.next();
            int chunkX = (int) chunkKey;
            int chunkZ = (int) (chunkKey >> 32);
            chunkPositions.add(ChunkPosition.of(bukkitWorld, chunkX, chunkZ, false));
        }
        return chunkPositions;
    }

    @Override
    public void applyBlocks(org.bukkit.Chunk bukkitChunk) {
        if (baseLocationForCache != null)
            throw new IllegalStateException("Cannot call applyBlocks on WorldEditSession cache object");

        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        ChunkData chunkData = this.chunks.get(chunk.chunkKey);

        if (chunkData == null)
            return;

        int chunkSectionsCount = Math.min(chunkData.chunkSections.length, chunk.getSections().length);
        for (int i = 0; i < chunkSectionsCount; ++i) {
            chunk.getSections()[i] = chunkData.chunkSections[i];
        }

        // Update the biome for the chunk
        BiomeBase biome = CraftBlock.biomeToBiomeBase(IslandUtils.getDefaultWorldBiome(this.dimension));
        Arrays.fill(chunk.getBiomeIndex(), (byte) BiomeBase.REGISTRY_ID.a(biome));

        if (plugin.getSettings().isLightsUpdate()) {
            // initLightning calculates the light emitted from sky to the chunk.
            chunk.initLighting();
        }

        chunk.markDirty();
    }

    @Override
    public void finish(Island island) {
        if (baseLocationForCache != null)
            throw new IllegalStateException("Cannot call finish on WorldEditSession cache object");

        // Update blocks
        blocksToUpdate.forEach(data -> worldServer.setTypeAndData(data.getKey(), data.getValue(), 3));

        // Update block entities
        blockEntities.forEach(data -> {
            NBTTagCompound blockEntityCompound = (NBTTagCompound) data.getValue().toNBT();
            if (blockEntityCompound != null) {
                BlockPosition blockPosition = data.getKey();
                blockEntityCompound.setInt("x", blockPosition.getX());
                blockEntityCompound.setInt("y", blockPosition.getY());
                blockEntityCompound.setInt("z", blockPosition.getZ());
                TileEntity worldTileEntity = worldServer.getTileEntity(blockPosition);
                if (worldTileEntity != null) {
                    if (TILE_ENTITY_LOAD.isValid()) {
                        TILE_ENTITY_LOAD.invoke(worldTileEntity, blockEntityCompound);
                    } else {
                        worldTileEntity.load(blockEntityCompound);
                    }
                }
            }
        });

        if (plugin.getSettings().isLightsUpdate() && !lights.isEmpty()) {
            // For each light block, we calculate its light
            // We only update the lights after all the chunks were loaded.
            BukkitExecutor.sync(() -> {
                lights.forEach(blockPosition -> worldServer.c(EnumSkyBlock.BLOCK, blockPosition));

                LongIterator iterator = this.chunks.keyIterator();
                while (iterator.hasNext()) {
                    long chunkKey = iterator.next();
                    ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair((int) chunkKey, (int) (chunkKey >> 32));
                    NMSUtils.sendPacketToRelevantPlayers(worldServer, chunkCoord.x, chunkCoord.z,
                            new PacketPlayOutMapChunk(worldServer.getChunkAt(chunkCoord.x, chunkCoord.z), 65535));
                }
            }, 5L);
        }

        release();
    }

    @Override
    public void markForCache(Location location) {
        this.baseLocationForCache = location.clone();
    }

    @Override
    public WorldEditSession buildFromCache(Location location) {
        if (this.baseLocationForCache == null)
            throw new IllegalStateException("Cannot call buildFromCache for non-cache WorldEditSessions");

        int chunkPosXAxisDelta = (location.getBlockX() >> 4) - (baseLocationForCache.getBlockX() >> 4);
        int chunkPosZAxisDelta = (location.getBlockZ() >> 4) - (baseLocationForCache.getBlockZ() >> 4);
        int xAxisDelta = location.getBlockX() - baseLocationForCache.getBlockX();
        int zAxisDelta = location.getBlockZ() - baseLocationForCache.getBlockZ();

        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();

        WorldEditSessionImpl worldEditSession = obtain(worldServer);

        // We need to transform all data to the new base location values
        Iterator<Long2ObjectMapView.Entry<ChunkData>> chunksIterator = this.chunks.entryIterator();
        while (chunksIterator.hasNext()) {
            Long2ObjectMapView.Entry<ChunkData> entry = chunksIterator.next();
            long newPos = ChunkCoordIntPair.a(getChunkCoordX(entry.getKey()) + chunkPosXAxisDelta,
                    getChunkCoordZ(entry.getKey()) + chunkPosZAxisDelta);
            worldEditSession.chunks.put(newPos, entry.getValue());
        }

        this.blocksToUpdate.forEach(blockToUpdatePair -> {
            BlockPosition newPos = blockToUpdatePair.getKey().a(xAxisDelta, 0, zAxisDelta);
            worldEditSession.blocksToUpdate.add(new Pair<>(newPos, blockToUpdatePair.getValue()));
        });

        this.blockEntities.forEach(blockEntityPair -> {
            BlockPosition newPos = blockEntityPair.getKey().a(xAxisDelta, 0, zAxisDelta);
            worldEditSession.blockEntities.add(new Pair<>(newPos, blockEntityPair.getValue()));
        });

        this.lights.forEach(lightPosition -> {
            BlockPosition newPos = lightPosition.a(xAxisDelta, 0, zAxisDelta);
            worldEditSession.lights.add(newPos);
        });

        return worldEditSession;
    }

    @Override
    public void release() {
        if (this.baseLocationForCache != null)
            return;

        this.chunks.clear();
        this.blocksToUpdate.clear();
        this.blockEntities.clear();
        this.lights.clear();
        this.worldServer = null;
        this.dimension = null;
        POOL.release(this);
    }

    private boolean isValidPosition(BlockPosition blockPosition) {
        return blockPosition.getX() >= -30000000 && blockPosition.getZ() >= -30000000 &&
                blockPosition.getX() < 30000000 && blockPosition.getZ() < 30000000 &&
                blockPosition.getY() >= 0 && blockPosition.getY() < worldServer.getHeight();
    }

    private static int getChunkCoordX(long i) {
        return (int) (i & 4294967295L);
    }

    private static int getChunkCoordZ(long i) {
        return (int) (i >>> 32 & 4294967295L);
    }

    private class ChunkData {

        private final ChunkSection[] chunkSections = new ChunkSection[16];

        public ChunkData(long chunkKey) {
            ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair((int) chunkKey, (int) (chunkKey >> 32));
            createChunkSections();
            runCustomWorldGenerator(chunkCoord);
        }

        private void createChunkSections() {
            for (int i = 0; i < this.chunkSections.length; ++i) {
                int chunkSectionPos = i << 4;
                this.chunkSections[i] = EmptyCounterChunkSection.of(new ChunkSection(chunkSectionPos, worldServer.worldProvider.m()));
            }
        }

        private void runCustomWorldGenerator(ChunkCoordIntPair chunkCoord) {
            ChunkGenerator bukkitGenerator = worldServer.getWorld().getGenerator();

            if (bukkitGenerator == null || bukkitGenerator instanceof IslandsGenerator)
                return;

            CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(worldServer, worldServer.getSeed(), bukkitGenerator);
            Chunk generatedChunk = chunkGenerator.getOrCreateChunk(chunkCoord.x, chunkCoord.z);

            for (int i = 0; i < this.chunkSections.length; ++i) {
                ChunkSection generatorChunkSection = generatedChunk.getSections()[i];
                if (generatorChunkSection != null && generatorChunkSection != Chunk.a)
                    this.chunkSections[i] = generatorChunkSection;
            }
        }

    }

}
