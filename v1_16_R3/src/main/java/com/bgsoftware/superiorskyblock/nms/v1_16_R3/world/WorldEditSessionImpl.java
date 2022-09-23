package com.bgsoftware.superiorskyblock.nms.v1_16_R3.world;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BiomeStorage;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockBed;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkConverter;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.ChunkStatus;
import net.minecraft.server.v1_16_R3.FluidType;
import net.minecraft.server.v1_16_R3.FluidTypes;
import net.minecraft.server.v1_16_R3.HeightMap;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IBlockState;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.LightEngine;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.ProtoChunk;
import net.minecraft.server.v1_16_R3.ProtoChunkTickList;
import net.minecraft.server.v1_16_R3.RegionLimitedWorldAccess;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorldEditSessionImpl implements WorldEditSession {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(
            BiomeStorage.class, BiomeBase[].class, "h");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Map<Long, ChunkData> chunks = new HashMap<>();
    private final List<Pair<BlockPosition, IBlockData>> blocksToUpdate = new LinkedList<>();
    private final List<BlockPosition> lights = new LinkedList<>();
    private final List<Pair<BlockPosition, CompoundTag>> blockEntities = new LinkedList<>();

    private final WorldServer worldServer;

    public WorldEditSessionImpl(WorldServer worldServer) {
        this.worldServer = worldServer;
    }

    @Override
    public void setBlock(Location location, int combinedId, @Nullable CompoundTag statesTag,
                         @Nullable CompoundTag blockEntityData) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (!isValidPosition(blockPosition))
            return;

        IBlockData blockData = Block.getByCombinedId(combinedId);

        if (statesTag != null) {
            for (Map.Entry<String, Tag<?>> entry : statesTag.entrySet()) {
                try {
                    // noinspection rawtypes
                    IBlockState blockState = BlockStatesMapper.getBlockState(entry.getKey());
                    if (blockState != null) {
                        if (entry.getValue() instanceof ByteTag) {
                            // noinspection unchecked
                            blockData = blockData.set(blockState, ((ByteTag) entry.getValue()).getValue() == 1);
                        } else if (entry.getValue() instanceof IntArrayTag) {
                            int[] data = ((IntArrayTag) entry.getValue()).getValue();
                            // noinspection unchecked
                            blockData = blockData.set(blockState, data[0]);
                        } else if (entry.getValue() instanceof StringTag) {
                            String data = ((StringTag) entry.getValue()).getValue();
                            // noinspection unchecked
                            blockData = blockData.set(blockState, Enum.valueOf(blockState.getType(), data));
                        }
                    }
                } catch (Exception error) {
                    PluginDebugger.debug(error);
                }
            }
        }

        if ((blockData.getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) ||
                blockData.getBlock() instanceof BlockBed) {
            blocksToUpdate.add(new Pair<>(blockPosition, blockData));
            return;
        }

        ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(blockPosition);

        if (blockData.f() > 0)
            lights.add(blockPosition);

        if (blockEntityData != null)
            blockEntities.add(new Pair<>(blockPosition, blockEntityData));

        ChunkData chunkData = this.chunks.computeIfAbsent(chunkCoord.pair(), ChunkData::new);

        ChunkSection chunkSection = chunkData.chunkSections[blockPosition.getY() >> 4];

        int blockX = blockPosition.getX() & 15;
        int blockY = blockPosition.getY();
        int blockZ = blockPosition.getZ() & 15;

        chunkSection.setType(blockX, blockY & 15, blockZ, blockData, false);

        chunkData.heightmaps.get(HeightMap.Type.MOTION_BLOCKING).a(blockX, blockY, blockZ, blockData);
        chunkData.heightmaps.get(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES).a(blockX, blockY, blockZ, blockData);
        chunkData.heightmaps.get(HeightMap.Type.OCEAN_FLOOR).a(blockX, blockY, blockZ, blockData);
        chunkData.heightmaps.get(HeightMap.Type.WORLD_SURFACE).a(blockX, blockY, blockZ, blockData);
    }

    @Override
    public List<ChunkPosition> getAffectedChunks() {
        World bukkitWorld = worldServer.getWorld();
        return new SequentialListBuilder<Long>().map(chunks.keySet(), chunkKey -> {
            ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(chunkKey);
            return ChunkPosition.of(bukkitWorld, chunkCoord.x, chunkCoord.z);
        });
    }

    @Override
    public void applyBlocks(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        ChunkData chunkData = this.chunks.remove(chunk.getPos().pair());

        if (chunkData == null)
            return;

        int chunkSectionsCount = Math.min(chunkData.chunkSections.length, chunk.getSections().length);
        for (int i = 0; i < chunkSectionsCount; ++i) {
            chunk.getSections()[i] = chunkData.chunkSections[i];
        }

        chunkData.heightmaps.forEach(((type, heightmap) -> {
            chunk.a(type, heightmap.a());
        }));

        // Update the biome for the chunk
        BiomeBase[] biomes = BIOME_BASE_ARRAY.get(chunk.getBiomeIndex());
        if (biomes != null) {
            IRegistry<BiomeBase> biomesRegistry = worldServer.r().b(IRegistry.ay);
            BiomeBase biome = CraftBlock.biomeToBiomeBase(biomesRegistry,
                    IslandUtils.getDefaultWorldBiome(worldServer.getWorld().getEnvironment()));
            Arrays.fill(biomes, biome);
        }
    }

    @Override
    public void finish(Island island) {
        // Update blocks
        blocksToUpdate.forEach(data -> worldServer.setTypeAndData(data.getKey(), data.getValue(), 3));

        // Update lights
        LightEngine lightEngine = worldServer.e();
        lights.forEach(lightEngine::a);

        // Update block entities
        blockEntities.forEach(data -> {
            NBTTagCompound blockEntityCompound = (NBTTagCompound) data.getValue().toNBT();
            if (blockEntityCompound != null) {
                BlockPosition blockPosition = data.getKey();
                blockEntityCompound.setInt("x", blockPosition.getX());
                blockEntityCompound.setInt("y", blockPosition.getY());
                blockEntityCompound.setInt("z", blockPosition.getZ());
                TileEntity worldTileEntity = worldServer.getTileEntity(blockPosition);
                if (worldTileEntity != null)
                    worldTileEntity.load(worldServer.getType(blockPosition), blockEntityCompound);
            }
        });
    }

    private boolean isValidPosition(BlockPosition blockPosition) {
        return blockPosition.getX() >= -30000000 && blockPosition.getZ() >= -30000000 &&
                blockPosition.getX() < 30000000 && blockPosition.getZ() < 30000000 &&
                blockPosition.getY() >= 0 && blockPosition.getY() < worldServer.getHeight();
    }

    private class ChunkData {
        private final ChunkSection[] chunkSections = new ChunkSection[16];
        private final Map<HeightMap.Type, HeightMap> heightmaps = new EnumMap<>(HeightMap.Type.class);

        public ChunkData(long chunkKey) {
            ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(chunkKey);

            createChunkSections();

            ProtoChunkTickList<Block> blockTickScheduler = new ProtoChunkTickList<>(block -> {
                return block == null || block.getBlockData().isAir();
            }, chunkCoord);
            ProtoChunkTickList<FluidType> fluidTickScheduler = new ProtoChunkTickList<>((fluid) -> {
                return fluid == null || fluid == FluidTypes.EMPTY;
            }, chunkCoord);

            ProtoChunk tempChunk;

            try {
                tempChunk = new ProtoChunk(chunkCoord, ChunkConverter.a, this.chunkSections, blockTickScheduler, fluidTickScheduler, worldServer);
            } catch (Throwable error) {
                tempChunk = new ProtoChunk(chunkCoord, ChunkConverter.a, this.chunkSections, blockTickScheduler, fluidTickScheduler);
            }

            createHeightmaps(tempChunk);
            runCustomWorldGenerator(tempChunk);
        }

        private void createChunkSections() {
            for (int i = 0; i < this.chunkSections.length; ++i) {
                int chunkSectionPos = i << 4;

                try {
                    this.chunkSections[i] = new ChunkSection(chunkSectionPos, null, worldServer, true);
                } catch (Throwable error) {
                    this.chunkSections[i] = new ChunkSection(chunkSectionPos);
                }
            }
        }

        private void runCustomWorldGenerator(ProtoChunk tempChunk) {
            ChunkGenerator bukkitGenerator = worldServer.getWorld().getGenerator();

            if (bukkitGenerator == null || bukkitGenerator instanceof IslandsGenerator)
                return;

            CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(worldServer,
                    worldServer.getChunkProvider().getChunkGenerator(),
                    bukkitGenerator);

            RegionLimitedWorldAccess region = new RegionLimitedWorldAccess(worldServer, Collections.singletonList(tempChunk));

            chunkGenerator.buildBase(region, tempChunk);

            // We want to copy the level chunk sections back
            System.arraycopy(tempChunk.getSections(), 0, this.chunkSections, 0, this.chunkSections.length);
        }

        private void createHeightmaps(ProtoChunk tempChunk) {
            for (HeightMap.Type heightmapType : HeightMap.Type.values()) {
                if (ChunkStatus.FULL.h().contains(heightmapType)) {
                    this.heightmaps.put(heightmapType, new HeightMap(tempChunk, heightmapType));
                }
            }
        }

    }

}
