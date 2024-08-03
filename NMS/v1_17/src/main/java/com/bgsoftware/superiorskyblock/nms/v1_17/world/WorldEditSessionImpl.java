package com.bgsoftware.superiorskyblock.nms.v1_17.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.LongIterator;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class WorldEditSessionImpl implements WorldEditSession {

    private static final ReflectField<Biome[]> BIOME_BASE_ARRAY = new ReflectField<>(
            ChunkBiomeContainer.class, Biome[].class, "f");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final boolean isStarLightInterface = ((Supplier<Boolean>) () -> {
        try {
            Class.forName("ca.spottedleaf.starlight.common.light.StarLightInterface");
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        }
    }).get();

    private final Long2ObjectMapView<ChunkData> chunks = CollectionsFactory.createLong2ObjectArrayMap();
    private final List<Pair<BlockPos, BlockState>> blocksToUpdate = new LinkedList<>();
    private final List<Pair<BlockPos, CompoundTag>> blockEntities = new LinkedList<>();
    private final Set<ChunkPos> lightenChunks = isStarLightInterface ? new HashSet<>() : Collections.emptySet();
    private final ServerLevel serverLevel;
    private final Dimension dimension;

    public WorldEditSessionImpl(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        this.dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(serverLevel.getWorld());
    }

    @Override
    public void setBlock(Location location, int combinedId, @Nullable CompoundTag statesTag,
                         @Nullable CompoundTag blockEntityData) {
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (!isValidPosition(blockPos))
            return;

        BlockState blockState = Block.stateById(combinedId);

        if (statesTag != null) {
            for (Map.Entry<String, Tag<?>> entry : statesTag.entrySet()) {
                try {
                    // noinspection rawtypes
                    Property property = PropertiesMapper.getProperty(entry.getKey());
                    if (property != null) {
                        if (entry.getValue() instanceof ByteTag) {
                            // noinspection unchecked
                            blockState = blockState.setValue(property, ((ByteTag) entry.getValue()).getValue() == 1);
                        } else if (entry.getValue() instanceof IntArrayTag) {
                            int[] data = ((IntArrayTag) entry.getValue()).getValue();
                            // noinspection unchecked
                            blockState = blockState.setValue(property, data[0]);
                        } else if (entry.getValue() instanceof StringTag) {
                            String data = ((StringTag) entry.getValue()).getValue();
                            // noinspection unchecked
                            blockState = blockState.setValue(property, Enum.valueOf(property.getValueClass(), data));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if ((blockState.getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) ||
                blockState.getBlock() instanceof BedBlock) {
            blocksToUpdate.add(new Pair<>(blockPos, blockState));
            return;
        }

        ChunkPos chunkPos = new ChunkPos(blockPos);

        if (blockEntityData != null)
            blockEntities.add(new Pair<>(blockPos, blockEntityData));

        ChunkData chunkData = this.chunks.computeIfAbsent(chunkPos.toLong(), ChunkData::new);

        if (plugin.getSettings().isLightsUpdate() && !isStarLightInterface && blockState.getLightEmission() > 0)
            chunkData.lights.add(blockPos);

        LevelChunkSection levelChunkSection = chunkData.chunkSections[serverLevel.getSectionIndex(blockPos.getY())];

        int blockX = blockPos.getX() & 15;
        int blockY = blockPos.getY();
        int blockZ = blockPos.getZ() & 15;

        levelChunkSection.setBlockState(blockX, blockY & 15, blockZ, blockState, false);

        chunkData.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(blockX, blockY, blockZ, blockState);
        chunkData.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(blockX, blockY, blockZ, blockState);
        chunkData.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(blockX, blockY, blockZ, blockState);
        chunkData.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(blockX, blockY, blockZ, blockState);
    }

    @Override
    public List<ChunkPosition> getAffectedChunks() {
        List<ChunkPosition> chunkPositions = new LinkedList<>();
        World bukkitWorld = serverLevel.getWorld();
        LongIterator iterator = chunks.keyIterator();
        while (iterator.hasNext()) {
            long chunkKey = iterator.next();
            ChunkPos chunkPos = new ChunkPos(chunkKey);
            chunkPositions.add(ChunkPosition.of(bukkitWorld, chunkPos.x, chunkPos.z));
        }
        return chunkPositions;
    }

    @Override
    public void applyBlocks(Chunk bukkitChunk) {
        LevelChunk levelChunk = ((CraftChunk) bukkitChunk).getHandle();
        ChunkPos chunkPos = levelChunk.getPos();

        ChunkData chunkData = this.chunks.remove(chunkPos.toLong());

        if (chunkData == null)
            return;

        int chunkSectionsCount = Math.min(chunkData.chunkSections.length, levelChunk.getSections().length);
        for (int i = 0; i < chunkSectionsCount; ++i) {
            levelChunk.getSections()[i] = chunkData.chunkSections[i];
        }

        chunkData.heightmaps.forEach(((type, heightmap) -> {
            levelChunk.setHeightmap(type, heightmap.getRawData());
        }));

        // Update the biome for the chunk
        Biome[] biomes = BIOME_BASE_ARRAY.get(levelChunk.getBiomes());
        if (biomes != null) {
            Registry<Biome> biomesRegistry = serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
            Biome biome = CraftBlock.biomeToBiomeBase(biomesRegistry, IslandUtils.getDefaultWorldBiome(this.dimension));
            Arrays.fill(biomes, biome);
        }

        if (plugin.getSettings().isLightsUpdate()) {
            if (isStarLightInterface) {
                this.lightenChunks.add(chunkPos);
            } else {
                ThreadedLevelLightEngine threadedLevelLightEngine = serverLevel.getChunkSource().getLightEngine();
                chunkData.lights.forEach(threadedLevelLightEngine::checkBlock);
                // Queues chunk light for this chunk.
                threadedLevelLightEngine.lightChunk(levelChunk, false);
            }
        }

        levelChunk.setUnsaved(true);
    }

    @Override
    public void finish(Island island) {
        // Update blocks
        blocksToUpdate.forEach(data -> serverLevel.setBlock(data.getKey(), data.getValue(), 3));

        // Update block entities
        blockEntities.forEach(data -> {
            net.minecraft.nbt.CompoundTag blockEntityCompound = (net.minecraft.nbt.CompoundTag) data.getValue().toNBT();
            if (blockEntityCompound != null) {
                BlockPos blockPos = data.getKey();
                blockEntityCompound.putInt("x", blockPos.getX());
                blockEntityCompound.putInt("y", blockPos.getY());
                blockEntityCompound.putInt("z", blockPos.getZ());

                if (blockEntityCompound.getByte("SSB.HasSignLines") == 1) {
                    // We want to convert the sign lines from raw string to json
                    for (int i = 1; i <= 4; ++i) {
                        String line = blockEntityCompound.getString("SSB.Text" + i);
                        if (!Text.isBlank(line)) {
                            Component newLine = CraftChatMessage.fromString(line)[0];
                            blockEntityCompound.putString("Text" + i, Component.Serializer.toJson(newLine));
                        }
                    }
                }

                BlockEntity worldBlockEntity = serverLevel.getBlockEntity(blockPos);
                if (worldBlockEntity != null)
                    worldBlockEntity.load(blockEntityCompound);
            }
        });

        if (plugin.getSettings().isLightsUpdate() && isStarLightInterface && !lightenChunks.isEmpty()) {
            ThreadedLevelLightEngine threadedLevelLightEngine = serverLevel.getChunkSource().getLightEngine();
            threadedLevelLightEngine.relight(lightenChunks, chunkCallback -> {
            }, completeCallback -> {
            });
            this.lightenChunks.clear();
        }
    }

    private boolean isValidPosition(BlockPos blockPos) {
        return blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 &&
                blockPos.getX() < 30000000 && blockPos.getZ() < 30000000 &&
                blockPos.getY() >= serverLevel.getMinBuildHeight() && blockPos.getY() < serverLevel.getMaxBuildHeight();
    }

    private class ChunkData {
        private final LevelChunkSection[] chunkSections = new LevelChunkSection[serverLevel.getSectionsCount()];
        private final Map<Heightmap.Types, Heightmap> heightmaps = new EnumMap<>(Heightmap.Types.class);
        private final List<BlockPos> lights = new LinkedList<>();

        public ChunkData(long chunkKey) {
            ChunkPos chunkPos = new ChunkPos(chunkKey);

            createChunkSections();

            ProtoTickList<Block> blockTickScheduler = new ProtoTickList<>(block -> {
                return block == null || block.defaultBlockState().isAir();
            }, chunkPos, serverLevel);
            ProtoTickList<Fluid> fluidTickScheduler = new ProtoTickList<>((fluid) -> {
                return fluid == null || fluid == Fluids.EMPTY;
            }, chunkPos, serverLevel);

            ProtoChunk tempChunk;

            try {
                tempChunk = new ProtoChunk(chunkPos, UpgradeData.EMPTY, this.chunkSections, blockTickScheduler, fluidTickScheduler, serverLevel, serverLevel);
            } catch (Throwable error) {
                tempChunk = new ProtoChunk(chunkPos, UpgradeData.EMPTY, this.chunkSections, blockTickScheduler, fluidTickScheduler, serverLevel);
            }

            createHeightmaps(tempChunk);
            runCustomWorldGenerator(tempChunk);
        }

        private void createChunkSections() {
            for (int i = 0; i < this.chunkSections.length; ++i) {
                int chunkSectionPos = serverLevel.getSectionYFromSectionIndex(i);

                try {
                    this.chunkSections[i] = new LevelChunkSection(chunkSectionPos, null, serverLevel, true);
                } catch (Throwable error) {
                    this.chunkSections[i] = new LevelChunkSection(chunkSectionPos);
                }
            }
        }

        private void runCustomWorldGenerator(ProtoChunk tempChunk) {
            ChunkGenerator bukkitGenerator = serverLevel.getWorld().getGenerator();

            if (bukkitGenerator == null || bukkitGenerator instanceof IslandsGenerator)
                return;

            CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(serverLevel,
                    serverLevel.getChunkSource().getGenerator(),
                    bukkitGenerator);

            WorldGenRegion region = new WorldGenRegion(serverLevel, Collections.singletonList(tempChunk),
                    ChunkStatus.SURFACE, 0);

            chunkGenerator.buildSurface(region,
                    tempChunk);

            // We want to copy the level chunk sections back
            LevelChunkSection[] tempChunkSections = tempChunk.getSections();
            for (int i = 0; i < Math.min(this.chunkSections.length, tempChunkSections.length); ++i) {
                LevelChunkSection chunkSection = tempChunkSections[i];
                if (chunkSection != null && chunkSection != LevelChunk.EMPTY_SECTION)
                    this.chunkSections[i] = chunkSection;
            }
        }

        private void createHeightmaps(ProtoChunk tempChunk) {
            for (Heightmap.Types heightmapType : Heightmap.Types.values()) {
                if (ChunkStatus.FULL.heightmapsAfter().contains(heightmapType)) {
                    this.heightmaps.put(heightmapType, new Heightmap(tempChunk, heightmapType));
                }
            }
        }

    }

}
