package com.bgsoftware.superiorskyblock.nms.v1_20_4;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectMethod;
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
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.chunks.CropsBlockEntity;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NMSChunksImpl implements NMSChunks {

    private static final boolean hasStatesIterator = new ReflectMethod<>(PalettedContainer.class,
            "forEachLocation", PalettedContainer.CountConsumer.class).isValid();

    private final SuperiorSkyblockPlugin plugin;

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        KeyBlocksCache.cacheAllBlocks();
    }

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, org.bukkit.block.Biome bukkitBiome,
                         Collection<org.bukkit.entity.Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        Holder<Biome> biome = CraftBiome.bukkitToMinecraftHolder(bukkitBiome);

        NMSUtils.runActionOnChunks(chunkPositions, true, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                Registry<Biome> biomesRegistry = levelChunk.level.registryAccess().registryOrThrow(Registries.BIOME);

                ChunkPos chunkPos = levelChunk.getPos();

                LevelChunkSection[] chunkSections = levelChunk.getSections();
                for (int i = 0; i < chunkSections.length; ++i) {
                    LevelChunkSection currentSection = chunkSections[i];
                    if (currentSection != null) {
                        PalettedContainer<Holder<Biome>> biomesContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                                biome, PalettedContainer.Strategy.SECTION_BIOMES);
                        chunkSections[i] = new LevelChunkSection(currentSection.getStates(), biomesContainer);
                    }
                }

                levelChunk.setUnsaved(true);

                ClientboundForgetLevelChunkPacket forgetLevelChunkPacket = new ClientboundForgetLevelChunkPacket(chunkPos);
                ClientboundLevelChunkWithLightPacket mapChunkPacket = new ClientboundLevelChunkWithLightPacket(
                        levelChunk, levelChunk.level.getLightEngine(), null, null);

                playersToUpdate.forEach(player -> {
                    ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                    serverPlayer.connection.send(forgetLevelChunkPacket);
                    serverPlayer.connection.send(mapChunkPacket);
                });
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                Registry<Biome> biomesRegistry = unloadedChunkCompound.serverLevel().registryAccess().registryOrThrow(Registries.BIOME);

                Codec<PalettedContainer<Holder<Biome>>> biomesCodec = PalettedContainer.codecRW(
                        biomesRegistry.asHolderIdMap(),
                        biomesRegistry.holderByNameCodec(),
                        PalettedContainer.Strategy.SECTION_BIOMES,
                        biomesRegistry.getHolderOrThrow(Biomes.PLAINS)
                );
                PalettedContainer<Holder<Biome>> biomesContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                        biome, PalettedContainer.Strategy.SECTION_BIOMES);
                DataResult<Tag> dataResult = biomesCodec.encodeStart(NbtOps.INSTANCE, biomesContainer);
                Tag biomesCompound = dataResult.getOrThrow();

                ListTag sectionsList = unloadedChunkCompound.getSections();

                for (int i = 0; i < sectionsList.size(); ++i)
                    sectionsList.getCompound(i).put("biomes", biomesCompound);
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
            public void onLoadedChunk(LevelChunk levelChunk) {
                Registry<Biome> biomesRegistry = levelChunk.level.registryAccess().registryOrThrow(Registries.BIOME);

                LevelChunkSection[] chunkSections = levelChunk.getSections();
                for (int i = 0; i < chunkSections.length; ++i) {
                    chunkSections[i] = new LevelChunkSection(biomesRegistry);
                }

                removeEntities(levelChunk);

                levelChunk.blockEntities.keySet().clear();

                removeBlocks(levelChunk);
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();

                Codec<PalettedContainer<BlockState>> blocksCodec = PalettedContainer.codecRW(
                        Block.BLOCK_STATE_REGISTRY,
                        BlockState.CODEC,
                        PalettedContainer.Strategy.SECTION_STATES,
                        Blocks.AIR.defaultBlockState());

                ListTag tileEntities = new ListTag();

                unloadedChunkCompound.setEntities(new ListTag());
                unloadedChunkCompound.setBlockEntities(tileEntities);

                if (serverLevel.generator instanceof IslandsGenerator) {
                    PalettedContainer<BlockState> statesContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                            Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                    DataResult<Tag> dataResult = blocksCodec.encodeStart(NbtOps.INSTANCE, statesContainer);
                    Tag blockStatesCompound = dataResult.getOrThrow();

                    ListTag sectionsList = unloadedChunkCompound.getSections();
                    for (int i = 0; i < sectionsList.size(); ++i)
                        sectionsList.getCompound(i).put("block_states", blockStatesCompound);
                } else {
                    ProtoChunk protoChunk = NMSUtils.createProtoChunk(unloadedChunkCompound.chunkPos(), serverLevel);

                    try {
                        CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(serverLevel,
                                serverLevel.getChunkSource().getGenerator(), serverLevel.generator);

                        WorldGenRegion region = new WorldGenRegion(serverLevel, Collections.singletonList(protoChunk),
                                ChunkStatus.SURFACE, 0);

                        customChunkGenerator.buildSurface(region,
                                serverLevel.structureManager().forWorldGenRegion(region),
                                serverLevel.getChunkSource().randomState(),
                                protoChunk);
                    } catch (Exception ignored) {
                    }

                    Registry<Biome> biomesRegistry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);
                    Codec<PalettedContainerRO<Holder<Biome>>> biomesCodec = PalettedContainer.codecRO(
                            biomesRegistry.asHolderIdMap(),
                            biomesRegistry.holderByNameCodec(),
                            PalettedContainer.Strategy.SECTION_BIOMES,
                            biomesRegistry.getHolderOrThrow(Biomes.PLAINS)
                    );

                    LevelLightEngine lightEngine = serverLevel.getLightEngine();
                    LevelChunkSection[] chunkSections = protoChunk.getSections();

                    ListTag sectionsList = new ListTag();

                    // Save blocks
                    for (int i = lightEngine.getMinLightSection(); i < lightEngine.getMaxLightSection(); ++i) {
                        int chunkSectionIndex = serverLevel.getSectionIndex(i);

                        CompoundTag sectionCompound = new CompoundTag();

                        if (chunkSectionIndex >= 0 && chunkSectionIndex < chunkSections.length) {
                            LevelChunkSection levelChunkSection = chunkSections[chunkSectionIndex];

                            {
                                DataResult<Tag> dataResult = blocksCodec.encodeStart(NbtOps.INSTANCE, levelChunkSection.getStates());
                                sectionCompound.put("block_states", dataResult.getOrThrow());
                            }

                            {
                                DataResult<Tag> dataResult = biomesCodec.encodeStart(NbtOps.INSTANCE, levelChunkSection.getBiomes());
                                sectionCompound.put("biomes", dataResult.getOrThrow());
                            }
                        }

                        if (!sectionCompound.isEmpty()) {
                            sectionCompound.putByte("Y", (byte) i);
                            sectionsList.add(sectionCompound);
                        }
                    }

                    for (BlockPos blockEntityPos : protoChunk.blockEntities.keySet()) {
                        CompoundTag blockEntityCompound = protoChunk.getBlockEntityNbtForSaving(blockEntityPos,
                                MinecraftServer.getServer().registryAccess());
                        if (blockEntityCompound != null)
                            tileEntities.add(blockEntityCompound);
                    }

                    unloadedChunkCompound.setSections(sectionsList);
                }
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
            public void onLoadedChunk(LevelChunk levelChunk) {
                ChunkPos chunkPos = levelChunk.getPos();
                ChunkPosition chunkPosition = ChunkPosition.of(levelChunk.level.getWorld(), chunkPos.x, chunkPos.z);
                allCalculatedChunks.add(calculateChunk(chunkPosition, levelChunk.level, levelChunk.getSections()));
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                ChunkPosition chunkPosition = unloadedChunkCompound.chunkPosition();
                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();
                Registry<Biome> biomesRegistry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);

                Codec<PalettedContainer<BlockState>> blocksCodec = PalettedContainer.codecRW(
                        Block.BLOCK_STATE_REGISTRY,
                        BlockState.CODEC,
                        PalettedContainer.Strategy.SECTION_STATES,
                        Blocks.AIR.defaultBlockState()
                );
                Codec<PalettedContainer<Holder<Biome>>> biomesCodec = PalettedContainer.codecRW(
                        biomesRegistry.asHolderIdMap(),
                        biomesRegistry.holderByNameCodec(),
                        PalettedContainer.Strategy.SECTION_BIOMES,
                        biomesRegistry.getHolderOrThrow(Biomes.PLAINS)
                );

                LevelChunkSection[] chunkSections = new LevelChunkSection[serverLevel.getSectionsCount()];

                ListTag sectionsList = unloadedChunkCompound.getSections();
                for (int i = 0; i < sectionsList.size(); ++i) {
                    CompoundTag sectionCompound = sectionsList.getCompound(i);
                    byte yPosition = sectionCompound.getByte("Y");
                    int sectionIndex = serverLevel.getSectionIndexFromSectionY(yPosition);

                    if (sectionIndex >= 0 && sectionIndex < chunkSections.length) {
                        PalettedContainer<BlockState> blocksDataPalette;
                        if (sectionCompound.contains("block_states", 10)) {
                            DataResult<PalettedContainer<BlockState>> dataResult = blocksCodec.parse(NbtOps.INSTANCE,
                                    sectionCompound.getCompound("block_states")).promotePartial((sx) -> {
                            });
                            blocksDataPalette = dataResult.getOrThrow();
                        } else {
                            blocksDataPalette = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                                    Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                        }

                        PalettedContainer<Holder<Biome>> biomesDataPalette;
                        if (sectionCompound.contains("biomes", 10)) {
                            DataResult<PalettedContainer<Holder<Biome>>> dataResult = biomesCodec.parse(NbtOps.INSTANCE,
                                    sectionCompound.getCompound("biomes")).promotePartial((sx) -> {
                            });
                            biomesDataPalette = dataResult.getOrThrow();
                        } else {
                            biomesDataPalette = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                                    biomesRegistry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                        }

                        chunkSections[sectionIndex] = new LevelChunkSection(blocksDataPalette, biomesDataPalette);
                    }

                }

                CalculatedChunk calculatedChunk = calculateChunk(chunkPosition, serverLevel, chunkSections);
                allCalculatedChunks.add(calculatedChunk);
                unloadedChunksCache.write(m -> m.put(chunkPosition, calculatedChunk));
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
        List<NMSUtils.UnloadedChunkCompound> unloadedChunkCompounds = new LinkedList<>();

        NMSUtils.runActionOnEntityChunks(chunkPositions, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                for (org.bukkit.entity.Entity bukkitEntity : new CraftChunk(levelChunk).getEntities()) {
                    if (!BukkitEntities.canBypassEntityLimit(bukkitEntity))
                        chunkEntities.computeIfAbsent(Keys.of(bukkitEntity), i -> new Counter(0)).inc(1);
                }
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                unloadedChunkCompounds.add(unloadedChunkCompound);
            }

            @Override
            public void onFinish() {
                BukkitExecutor.sync(() -> {
                    for (NMSUtils.UnloadedChunkCompound unloadedChunkCompound : unloadedChunkCompounds) {
                        ServerLevel serverLevel = unloadedChunkCompound.serverLevel();

                        for (Tag entityTag : unloadedChunkCompound.getEntities()) {
                            EntityType<?> entityType = EntityType.by((CompoundTag) entityTag).orElse(null);
                            if (entityType == null)
                                continue;

                            Entity fakeEntity = EntityType.create((CompoundTag) entityTag, serverLevel).orElse(null);
                            if (fakeEntity != null) {
                                fakeEntity.valid = false;
                                if (BukkitEntities.canBypassEntityLimit(fakeEntity.getBukkitEntity()))
                                    continue;
                            }

                            Key entityKey = Keys.of(CraftEntityType.minecraftToBukkit(entityType));
                            chunkEntities.computeIfAbsent(entityKey, k -> new Counter(0)).inc(1);
                        }
                    }

                    completableFuture.complete(chunkEntities);
                });
            }
        });

        return completableFuture;
    }

    @Override
    public void injectChunkSections(Chunk chunk) {
        // No implementation
    }

    @Override
    public boolean isChunkEmpty(Chunk bukkitChunk) {
        LevelChunk levelChunk = NMSUtils.getCraftChunkHandle((CraftChunk) bukkitChunk);
        return Arrays.stream(levelChunk.getSections()).allMatch(chunkSection ->
                chunkSection == null || chunkSection.hasOnlyAir());
    }

    @Override
    public Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
        ChunkAccess chunkAccess = serverLevel.getChunkSource().getChunk(chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunkAccess instanceof LevelChunk levelChunk ? new CraftChunk(levelChunk) : null;
    }

    @Override
    public void startTickingChunk(Island island, Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;


        if (stop) {
            CropsBlockEntity cropsBlockEntity = CropsBlockEntity.remove(ChunkPos.asLong(chunk.getX(), chunk.getZ()));
            if (cropsBlockEntity != null)
                cropsBlockEntity.remove();
        } else {
            LevelChunk levelChunk = NMSUtils.getCraftChunkHandle((CraftChunk) chunk);
            CropsBlockEntity.create(island, levelChunk);
        }
    }

    @Override
    public void updateCropsTicker(List<ChunkPosition> chunkPositions, double newCropGrowthMultiplier) {
        if (chunkPositions.isEmpty()) return;
        CropsBlockEntity.forEachChunk(chunkPositions, cropsBlockEntity ->
                cropsBlockEntity.setCropGrowthMultiplier(newCropGrowthMultiplier));
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
    public List<Location> getBlockEntities(Chunk chunk) {
        LevelChunk levelChunk = NMSUtils.getCraftChunkHandle((CraftChunk) chunk);
        List<Location> blockEntities = new LinkedList<>();

        World bukkitWorld = chunk.getWorld();

        levelChunk.getBlockEntities().keySet().forEach(blockPos ->
                blockEntities.add(new Location(bukkitWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ())));

        return blockEntities;
    }

    private static CalculatedChunk calculateChunk(ChunkPosition chunkPosition, Level level, LevelChunkSection[] chunkSections) {
        KeyMap<Counter> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        List<Location> spawnersLocations = new LinkedList<>();

        for (int i = 0; i < chunkSections.length; ++i) {
            LevelChunkSection levelChunkSection = chunkSections[i];
            if (levelChunkSection != null && !levelChunkSection.hasOnlyAir()) {
                int sectionBottomY = level.getSectionYFromSectionIndex(i) << 4;
                if (hasStatesIterator) {
                    levelChunkSection.getStates().forEachLocation((blockState, locationKey) -> {
                        int x = locationKey & 0xF;
                        int y = (locationKey >> 8) & 0xF;
                        int z = (locationKey >> 4) & 0xF;
                        calculateChunkInternal(blockState, x, y, z, chunkPosition, sectionBottomY, blockCounts, spawnersLocations);
                    });
                } else for (BlockPos blockPos : BlockPos.betweenClosed(0, 0, 0, 15, 15, 15)) {
                    BlockState blockState = levelChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    calculateChunkInternal(blockState, blockPos.getX(), blockPos.getY(), blockPos.getZ(), chunkPosition,
                            sectionBottomY, blockCounts, spawnersLocations);
                }
            }
        }

        return new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations);
    }

    private static void calculateChunkInternal(BlockState blockState, int x, int y, int z, ChunkPosition chunkPosition,
                                               int sectionBottomY, KeyMap<Counter> blockCounts,
                                               List<Location> spawnersLocations) {
        Block block = blockState.getBlock();

        if (block == Blocks.AIR)
            return;

        Location location = new Location(chunkPosition.getWorld(),
                (chunkPosition.getX() << 4) + x,
                sectionBottomY + y,
                (chunkPosition.getZ() << 4) + z);

        int blockAmount = 1;

        if (NMSUtils.isDoubleBlock(block, blockState)) {
            blockAmount = 2;
            blockState = blockState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        }

        Key blockKey = Keys.of(KeyBlocksCache.getBlockKey(blockState.getBlock()), location);
        blockCounts.computeIfAbsent(blockKey, b -> new Counter(0)).inc(blockAmount);
        if (block == Blocks.SPAWNER) {
            spawnersLocations.add(location);
        }
    }

    private static void removeEntities(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        ServerLevel serverLevel = ((LevelChunk) chunk).level;

        int chunkWorldCoordX = chunkPos.x << 4;
        int chunkWorldCoordZ = chunkPos.z << 4;

        AABB chunkBounds = new AABB(chunkWorldCoordX, serverLevel.getMinBuildHeight(), chunkWorldCoordZ,
                chunkWorldCoordX + 15, serverLevel.getMaxBuildHeight(), chunkWorldCoordZ + 15);

        List<Entity> worldEntities = new LinkedList<>();
        serverLevel.getEntities().get(chunkBounds, worldEntities::add);

        worldEntities.forEach(nmsEntity -> {
            if (!(nmsEntity instanceof Player))
                nmsEntity.setRemoved(Entity.RemovalReason.DISCARDED);
        });
    }

    private static void removeBlocks(ChunkAccess chunk) {
        ServerLevel serverLevel = ((LevelChunk) chunk).level;

        ChunkGenerator bukkitGenerator = serverLevel.getWorld().getGenerator();

        if (bukkitGenerator != null && !(bukkitGenerator instanceof IslandsGenerator)) {
            CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(serverLevel,
                    serverLevel.getChunkSource().getGenerator(),
                    bukkitGenerator);

            WorldGenRegion region = new WorldGenRegion(serverLevel, Collections.singletonList(chunk),
                    ChunkStatus.SURFACE, 0);

            chunkGenerator.buildSurface(region,
                    serverLevel.structureManager().forWorldGenRegion(region),
                    serverLevel.getChunkSource().randomState(),
                    chunk);
        }
    }

}
