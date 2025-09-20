package com.bgsoftware.superiorskyblock.nms.v1_21_7.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.LongIterator;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.nms.v1_21_7.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.generator.ChunkGenerator;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class WorldEditSessionImpl implements WorldEditSession {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ObjectsPool<WorldEditSessionImpl> POOL = new ObjectsPool<>(WorldEditSessionImpl::new);

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final boolean isStarLightInterface = ((Supplier<Boolean>) () -> {
        try {
            Class.forName("ca.spottedleaf.moonrise.patches.starlight.light.StarLightInterface");
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        }
    }).get();

    private static final Component[] COMPONENT_ARRAY_TYPE = new Component[0];

    private final Long2ObjectMapView<ChunkData> chunks = CollectionsFactory.createLong2ObjectArrayMap();
    private final List<Pair<BlockPos, BlockState>> blocksToUpdate = new LinkedList<>();
    private final List<Pair<BlockPos, CompoundTag>> blockEntities = new LinkedList<>();
    private final Set<ChunkPos> lightenChunks = isStarLightInterface ? new HashSet<>() : Collections.emptySet();
    private ServerLevel serverLevel;
    private Dimension dimension;

    private Location baseLocationForCache = null;

    public static WorldEditSessionImpl obtain(ServerLevel serverLevel) {
        return POOL.obtain().initialize(serverLevel);
    }

    private WorldEditSessionImpl() {
    }

    public WorldEditSessionImpl initialize(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        this.dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(serverLevel.getWorld());
        return this;
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

        if ((blockState.liquid() && plugin.getSettings().isLiquidUpdate()) ||
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
        if (chunks.isEmpty())
            return Collections.emptyList();

        List<ChunkPosition> chunkPositions = new LinkedList<>();
        World bukkitWorld = serverLevel.getWorld();
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
    public void applyBlocks(Chunk bukkitChunk) {
        if (baseLocationForCache != null)
            throw new IllegalStateException("Cannot call applyBlocks on WorldEditSession cache object");

        LevelChunk levelChunk = Objects.requireNonNull(NMSUtils.getCraftChunkHandle((CraftChunk) bukkitChunk));
        ChunkPos chunkPos = levelChunk.getPos();

        long chunkKey = chunkPos.toLong();
        ChunkData chunkData = this.chunks.remove(chunkKey);

        if (chunkData == null)
            return;

        int chunkSectionsCount = Math.min(chunkData.chunkSections.length, levelChunk.getSections().length);
        for (int i = 0; i < chunkSectionsCount; ++i) {
            levelChunk.getSections()[i] = chunkData.chunkSections[i];
        }

        chunkData.heightmaps.forEach(((type, heightmap) -> {
            levelChunk.setHeightmap(type, heightmap.getRawData());
        }));

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

        levelChunk.markUnsaved();
    }

    @Override
    public void finish(Island island) {
        if (baseLocationForCache != null)
            throw new IllegalStateException("Cannot call applyBlocks on WorldEditSession cache object");

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

                applySignTextLines(blockEntityCompound, "front_text");
                applySignTextLines(blockEntityCompound, "back_text");
                convertLegacySignTextLines(blockEntityCompound);

                BlockEntity worldBlockEntity = serverLevel.getBlockEntity(blockPos);
                if (worldBlockEntity != null) {
                    try (ProblemReporter.ScopedCollector scopedCollector =
                                 new ProblemReporter.ScopedCollector(() -> "block_entity@" + blockPos, LOGGER)) {
                        ValueInput valueInput = TagValueInput.create(scopedCollector, serverLevel.registryAccess(), blockEntityCompound);
                        worldBlockEntity.loadWithComponents(valueInput);
                    }
                }
            }
        });

        if (plugin.getSettings().isLightsUpdate() && isStarLightInterface && !lightenChunks.isEmpty()) {
            ThreadedLevelLightEngine threadedLevelLightEngine = serverLevel.getChunkSource().getLightEngine();
            threadedLevelLightEngine.starlight$serverRelightChunks(lightenChunks, chunkCallback -> {
            }, completeCallback -> {
            });
            this.lightenChunks.clear();
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

        int chunkPosXAxisDelta = SectionPos.blockToSectionCoord(location.getBlockX()) - SectionPos.blockToSectionCoord(baseLocationForCache.getBlockX());
        int chunkPosZAxisDelta = SectionPos.blockToSectionCoord(location.getBlockZ()) - SectionPos.blockToSectionCoord(baseLocationForCache.getBlockZ());
        int xAxisDelta = location.getBlockX() - baseLocationForCache.getBlockX();
        int zAxisDelta = location.getBlockZ() - baseLocationForCache.getBlockZ();

        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();

        WorldEditSessionImpl worldEditSession = obtain(serverLevel);

        // We need to transform all data to the new base location values
        Iterator<Long2ObjectMapView.Entry<ChunkData>> chunksIterator = this.chunks.entryIterator();
        while (chunksIterator.hasNext()) {
            Long2ObjectMapView.Entry<ChunkData> entry = chunksIterator.next();
            long newPos = ChunkPos.asLong(ChunkPos.getX(entry.getKey()) + chunkPosXAxisDelta,
                    ChunkPos.getZ(entry.getKey()) + chunkPosZAxisDelta);

            LevelChunkSection[] sections = entry.getValue().chunkSections;
            Map<Heightmap.Types, Heightmap> heightmaps = entry.getValue().heightmaps;
            List<BlockPos> lights = entry.getValue().lights.isEmpty() ? Collections.emptyList() : new LinkedList<>();
            entry.getValue().lights.forEach(blockPos -> {
                lights.add(blockPos.offset(xAxisDelta, 0, zAxisDelta));
            });

            ChunkData newChunkData = new ChunkData(sections, heightmaps, lights);
            worldEditSession.chunks.put(newPos, newChunkData);
        }

        this.blocksToUpdate.forEach(blockToUpdatePair -> {
            BlockPos newPos = blockToUpdatePair.getKey().offset(xAxisDelta, 0, zAxisDelta);
            worldEditSession.blocksToUpdate.add(new Pair<>(newPos, blockToUpdatePair.getValue()));
        });

        this.blockEntities.forEach(blockEntityPair -> {
            BlockPos newPos = blockEntityPair.getKey().offset(xAxisDelta, 0, zAxisDelta);
            worldEditSession.blockEntities.add(new Pair<>(newPos, blockEntityPair.getValue()));
        });

        this.lightenChunks.forEach(lightenChunk -> {
            worldEditSession.lightenChunks.add(new ChunkPos(
                    lightenChunk.x + chunkPosXAxisDelta, lightenChunk.z + chunkPosZAxisDelta));
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
        this.lightenChunks.clear();
        this.serverLevel = null;
        this.dimension = null;
        POOL.release(this);
    }

    private boolean isValidPosition(BlockPos blockPos) {
        return blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 &&
                blockPos.getX() < 30000000 && blockPos.getZ() < 30000000 &&
                blockPos.getY() >= serverLevel.getMinY() && blockPos.getY() < serverLevel.getMaxY();
    }

    private static void applySignTextLines(net.minecraft.nbt.CompoundTag blockEntityCompound, String key) {
        blockEntityCompound.getCompound(key).ifPresent(textCompound -> {
            ListTag messages = textCompound.getListOrEmpty("messages");
            List<Component> textLines = new ArrayList<>();
            for (net.minecraft.nbt.Tag lineTag : messages) {
                try {
                    textLines.add(CraftChatMessage.fromJSON(lineTag.asString().orElseThrow()));
                } catch (JsonParseException error) {
                    textLines.add(CraftChatMessage.fromString(lineTag.asString().orElseThrow())[0]);
                }
            }

            for (int i = 0; i < 4; i++) {
                if (textLines.get(i) == null)
                    textLines.set(i, Component.empty());
            }

            Component[] textLinesArray = textLines.toArray(COMPONENT_ARRAY_TYPE);

            SignText signText = new SignText(textLinesArray, textLinesArray, DyeColor.BLACK, false);
            SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, signText).result()
                    .ifPresent(nbt -> blockEntityCompound.put(key, nbt));
        });
    }

    private static void convertLegacySignTextLines(net.minecraft.nbt.CompoundTag blockEntityCompound) {
        Component[] signLines = new Component[4];
        Arrays.fill(signLines, Component.empty());
        boolean hasAnySignLines = false;
        // We try to convert old text sign lines
        for (int i = 1; i <= 4; ++i) {
            if (blockEntityCompound.contains("SSB.Text" + i)) {
                String signLine = blockEntityCompound.getString("SSB.Text" + i).orElse(null);
                if (!Text.isBlank(signLine)) {
                    signLines[i - 1] = CraftChatMessage.fromString(signLine)[0];
                    hasAnySignLines = true;
                }
            } else {
                String signLine = blockEntityCompound.getString("Text" + i).orElse(null);
                if (!Text.isBlank(signLine)) {
                    signLines[i - 1] = CraftChatMessage.fromJSON(signLine);
                    hasAnySignLines = true;
                }
            }
        }

        if (hasAnySignLines) {
            SignText signText = new SignText(signLines, signLines, DyeColor.BLACK, false);
            SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, signText).result()
                    .ifPresent(frontTextNBT -> blockEntityCompound.put("front_text", frontTextNBT));
        }
    }

    private class ChunkData {

        private final LevelChunkSection[] chunkSections;
        private final Map<Heightmap.Types, Heightmap> heightmaps;
        private final List<BlockPos> lights;

        public ChunkData(long chunkKey) {
            this(new LevelChunkSection[serverLevel.getSectionsCount()], new EnumMap<>(Heightmap.Types.class),
                    isStarLightInterface ? Collections.emptyList() : new LinkedList<>());

            ChunkPos chunkPos = new ChunkPos(chunkKey);

            Registry<Biome> biomesRegistry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BIOME);

            createChunkSections(biomesRegistry);

            ProtoChunk tempChunk = new ProtoChunk(chunkPos, UpgradeData.EMPTY, this.chunkSections,
                    new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), serverLevel, biomesRegistry, null);

            createHeightmaps(tempChunk);
            runCustomWorldGenerator(tempChunk);
        }

        public ChunkData(LevelChunkSection[] chunkSections, Map<Heightmap.Types, Heightmap> heightmaps, List<BlockPos> lights) {
            this.chunkSections = chunkSections;
            this.heightmaps = heightmaps;
            this.lights = lights;
        }

        private void createChunkSections(Registry<Biome> biomesRegistry) {
            Holder<Biome> biome = CraftBiome.bukkitToMinecraftHolder(
                    IslandUtils.getDefaultWorldBiome(WorldEditSessionImpl.this.dimension));

            for (int i = 0; i < this.chunkSections.length; ++i) {
                PalettedContainer<Holder<Biome>> biomesContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                        biome, PalettedContainer.Strategy.SECTION_BIOMES);
                PalettedContainer<BlockState> statesContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                        Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);

                this.chunkSections[i] = new LevelChunkSection(statesContainer, biomesContainer);
            }
        }

        private void runCustomWorldGenerator(ProtoChunk tempChunk) {
            ChunkGenerator bukkitGenerator = serverLevel.getWorld().getGenerator();

            if (bukkitGenerator == null || bukkitGenerator instanceof IslandsGenerator)
                return;

            NMSUtils.buildSurfaceForChunk(serverLevel, tempChunk);

            // We want to copy the level chunk sections back
            LevelChunkSection[] tempChunkSections = tempChunk.getSections();
            for (int i = 0; i < Math.min(this.chunkSections.length, tempChunkSections.length); ++i) {
                LevelChunkSection chunkSection = tempChunkSections[i];
                if (chunkSection != null)
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
