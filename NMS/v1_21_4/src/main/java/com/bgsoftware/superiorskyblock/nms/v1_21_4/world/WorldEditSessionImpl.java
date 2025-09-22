package com.bgsoftware.superiorskyblock.nms.v1_21_4.world;

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
import com.bgsoftware.superiorskyblock.nms.v1_21_4.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.google.common.base.Preconditions;
import com.google.gson.JsonParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class WorldEditSessionImpl implements WorldEditSession {

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

    private Dimension dimension;
    private LevelHeightAccessor levelHeightAccessor;

    @Nullable
    private ServerLevel serverLevel;

    public static WorldEditSessionImpl obtain(ServerLevel serverLevel) {
        return POOL.obtain().initialize(serverLevel);
    }

    public static WorldEditSessionImpl obtain(Dimension dimension) {
        return POOL.obtain().initialize(dimension);
    }

    private WorldEditSessionImpl() {
    }

    private WorldEditSessionImpl initialize(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        this.dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(serverLevel.getWorld());
        this.levelHeightAccessor = serverLevel;
        return this;
    }

    private WorldEditSessionImpl initialize(Dimension dimension) {
        DimensionType dimensionType = getDimensionTypeFromDimension(dimension);
        this.serverLevel = null;
        this.dimension = dimension;
        this.levelHeightAccessor = LevelHeightAccessor.create(dimensionType.minY(), dimensionType.height());
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

        LevelChunkSection levelChunkSection = chunkData.chunkSections[this.levelHeightAccessor.getSectionIndex(blockPos.getY())];

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
        Preconditions.checkState(this.serverLevel != null, "Cannot call WorldEditSession#getAffectedChunks on partial initialized session");

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
        Preconditions.checkState(this.serverLevel != null, "Cannot call WorldEditSession#applyBlocks on partial initialized session");

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
        Preconditions.checkState(this.serverLevel != null, "Cannot call WorldEditSession#finish on partial initialized session");

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
                if (worldBlockEntity != null)
                    worldBlockEntity.loadWithComponents(blockEntityCompound, MinecraftServer.getServer().registryAccess());
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
    public Data readData(Location baseLocation) {
        return new WorldEditSessionDataImpl(baseLocation, this.chunks, this.blocksToUpdate, this.blockEntities, this.lightenChunks);
    }

    @Override
    public void applyData(Data data, Location baseLocation) {
        WorldEditSessionDataImpl dataImpl = (WorldEditSessionDataImpl) data;

        int baseBlockPosXAxis = baseLocation.getBlockX();
        int baseBlockPosYAxis = baseLocation.getBlockY();
        int baseBlockPosZAxis = baseLocation.getBlockZ();
        int baseChunkPosXAxis = baseBlockPosXAxis >> 4;
        int baseChunkPosZAxis = baseBlockPosZAxis >> 4;

        // We need to transform all data to the new base location values

        dataImpl.readChunks(baseChunkPosXAxis, baseChunkPosZAxis, baseBlockPosXAxis, baseBlockPosYAxis,
                baseBlockPosZAxis, this, this.chunks);
        dataImpl.readBlocksToUpdate(baseBlockPosXAxis, baseBlockPosYAxis, baseBlockPosZAxis, this.blocksToUpdate);
        dataImpl.readBlockEntities(baseBlockPosXAxis, baseBlockPosYAxis, baseBlockPosZAxis, this.blockEntities);
        dataImpl.readLights(baseChunkPosXAxis, baseChunkPosZAxis, this.lightenChunks);
    }

    @Override
    public void release() {
        this.chunks.clear();
        this.blocksToUpdate.clear();
        this.blockEntities.clear();
        this.lightenChunks.clear();
        this.serverLevel = null;
        this.dimension = null;
        this.levelHeightAccessor = null;
        POOL.release(this);
    }

    public ChunkData createChunkData(LevelChunkSection[] chunkSections, Map<Heightmap.Types, Heightmap> heightmaps, List<BlockPos> lights) {
        return new ChunkData(chunkSections, heightmaps, lights);
    }

    private boolean isValidPosition(BlockPos blockPos) {
        return blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 &&
                blockPos.getX() < 30000000 && blockPos.getZ() < 30000000 &&
                this.levelHeightAccessor.isInsideBuildHeight(blockPos.getY());
    }

    private static void applySignTextLines(net.minecraft.nbt.CompoundTag blockEntityCompound, String key) {
        if (blockEntityCompound.contains(key)) {
            net.minecraft.nbt.CompoundTag frontText = blockEntityCompound.getCompound(key);
            ListTag messages = frontText.getList("messages", net.minecraft.nbt.Tag.TAG_STRING);
            List<Component> textLines = new ArrayList<>();
            for (net.minecraft.nbt.Tag lineTag : messages) {
                try {
                    textLines.add(CraftChatMessage.fromJSON(lineTag.getAsString()));
                } catch (JsonParseException error) {
                    textLines.add(CraftChatMessage.fromString(lineTag.getAsString())[0]);
                }
            }

            while (textLines.size() < 4)
                textLines.add(Component.empty());

            Component[] textLinesArray = textLines.toArray(COMPONENT_ARRAY_TYPE);

            SignText signText = new SignText(textLinesArray, textLinesArray, DyeColor.BLACK, false);
            SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, signText).result()
                    .ifPresent(frontTextNBT -> blockEntityCompound.put(key, frontTextNBT));
        }
    }

    private static void convertLegacySignTextLines(net.minecraft.nbt.CompoundTag blockEntityCompound) {
        Component[] signLines = new Component[4];
        Arrays.fill(signLines, Component.empty());
        boolean hasAnySignLines = false;
        // We try to convert old text sign lines
        for (int i = 1; i <= 4; ++i) {
            if (blockEntityCompound.contains("SSB.Text" + i)) {
                String signLine = blockEntityCompound.getString("SSB.Text" + i);
                if (!Text.isBlank(signLine)) {
                    signLines[i - 1] = CraftChatMessage.fromString(signLine)[0];
                    hasAnySignLines = true;
                }
            } else {
                String signLine = blockEntityCompound.getString("Text" + i);
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

    private static DimensionType getDimensionTypeFromDimension(Dimension dimension) {
        ResourceKey<LevelStem> resourceKey;
        switch (dimension.getEnvironment()) {
            case NETHER -> resourceKey = LevelStem.NETHER;
            case THE_END -> resourceKey = LevelStem.END;
            default -> resourceKey = LevelStem.OVERWORLD;
        }
        Registry<LevelStem> registry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.LEVEL_STEM);
        return registry.getValue(resourceKey).type().value();
    }

    public class ChunkData {

        private final LevelChunkSection[] chunkSections;
        private final Map<Heightmap.Types, Heightmap> heightmaps;
        private final List<BlockPos> lights;

        private ChunkData(long chunkKey) {
            this(new LevelChunkSection[levelHeightAccessor.getSectionsCount()], new EnumMap<>(Heightmap.Types.class),
                    isStarLightInterface ? Collections.emptyList() : new LinkedList<>());
            ChunkPos chunkPos = new ChunkPos(chunkKey);

            Registry<Biome> biomesRegistry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BIOME);

            createChunkSections(biomesRegistry);

            ProtoChunk tempChunk = new ProtoChunk(chunkPos, UpgradeData.EMPTY, this.chunkSections,
                    new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), levelHeightAccessor, biomesRegistry, null);

            createHeightmaps(tempChunk);
            if (serverLevel != null)
                runCustomWorldGenerator(tempChunk);
        }

        private ChunkData(LevelChunkSection[] chunkSections, Map<Heightmap.Types, Heightmap> heightmaps, List<BlockPos> lights) {
            this.chunkSections = chunkSections;
            this.heightmaps = heightmaps;
            this.lights = lights;
        }

        public LevelChunkSection[] chunkSections() {
            return this.chunkSections;
        }

        public Map<Heightmap.Types, Heightmap> heightmaps() {
            return this.heightmaps;
        }

        public List<BlockPos> lights() {
            return this.lights;
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
