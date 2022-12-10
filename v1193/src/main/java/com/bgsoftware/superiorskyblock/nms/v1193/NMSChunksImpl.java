package com.bgsoftware.superiorskyblock.nms.v1193;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1193.chunks.CropsBlockEntity;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class NMSChunksImpl implements NMSChunks {

    private final SuperiorSkyblockPlugin plugin;

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, org.bukkit.block.Biome bukkitBiome,
                         Collection<org.bukkit.entity.Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkPos> chunksCoords = new SequentialListBuilder<ChunkPos>()
                .build(chunkPositions, chunkPosition -> new ChunkPos(chunkPosition.getX(), chunkPosition.getZ()));

        ServerLevel serverLevel = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();
        Registry<Biome> biomesRegistry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);

        Holder<Biome> biome = CraftBlock.biomeToBiomeBase(biomesRegistry, bukkitBiome);

        NMSUtils.runActionOnChunks(serverLevel, chunksCoords, true, null, chunkAccess -> {
            ChunkPos chunkPos = chunkAccess.getPos();

            LevelChunkSection[] chunkSections = chunkAccess.getSections();
            for (int i = 0; i < chunkSections.length; ++i) {
                LevelChunkSection currentSection = chunkSections[i];
                if (currentSection != null) {
                    PalettedContainer<Holder<Biome>> biomesContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                            biome, PalettedContainer.Strategy.SECTION_BIOMES);
                    chunkSections[i] = new LevelChunkSection(currentSection.bottomBlockY() >> 4,
                            currentSection.getStates(), biomesContainer);
                }
            }

            chunkAccess.setUnsaved(true);

            ClientboundForgetLevelChunkPacket forgetLevelChunkPacket = new ClientboundForgetLevelChunkPacket(chunkPos.x, chunkPos.z);
            ClientboundLevelChunkWithLightPacket mapChunkPacket = new ClientboundLevelChunkWithLightPacket(
                    (LevelChunk) chunkAccess, serverLevel.getLightEngine(), null, null, true);

            playersToUpdate.forEach(player -> {
                ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                serverPlayer.connection.send(forgetLevelChunkPacket);
                serverPlayer.connection.send(mapChunkPacket);
            });
        }, unloadedChunkCompound -> {
            Codec<PalettedContainer<Holder<Biome>>> biomesCodec = PalettedContainer.codecRW(
                    biomesRegistry.asHolderIdMap(),
                    biomesRegistry.holderByNameCodec(),
                    PalettedContainer.Strategy.SECTION_BIOMES,
                    biomesRegistry.getHolderOrThrow(Biomes.PLAINS)
            );
            PalettedContainer<Holder<Biome>> biomesContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                    biome, PalettedContainer.Strategy.SECTION_BIOMES);
            DataResult<Tag> dataResult = biomesCodec.encodeStart(NbtOps.INSTANCE, biomesContainer);
            Tag biomesCompound = dataResult.getOrThrow(false, error -> {
            });

            ListTag sectionsList = unloadedChunkCompound.getSections();

            for (int i = 0; i < sectionsList.size(); ++i)
                sectionsList.getCompound(i).put("biomes", biomesCompound);
        });
    }

    @Override
    public void deleteChunks(Island island, List<ChunkPosition> chunkPositions, Runnable onFinish) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkPos> chunksCoords = new SequentialListBuilder<ChunkPos>()
                .build(chunkPositions, chunkPosition -> new ChunkPos(chunkPosition.getX(), chunkPosition.getZ()));

        chunkPositions.forEach(chunkPosition -> island.markChunkEmpty(chunkPosition.getWorld(),
                chunkPosition.getX(), chunkPosition.getZ(), false));

        ServerLevel serverLevel = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();

        NMSUtils.runActionOnChunks(serverLevel, chunksCoords, true, onFinish, chunkAccess -> {
            Registry<Biome> biomesRegistry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);

            LevelChunkSection[] chunkSections = chunkAccess.getSections();
            for (int i = 0; i < chunkSections.length; ++i) {
                chunkSections[i] = new LevelChunkSection(serverLevel.getSectionYFromSectionIndex(i), biomesRegistry);
            }

            removeEntities(chunkAccess);

            chunkAccess.blockEntities.keySet().clear();

            removeBlocks(chunkAccess);
        }, unloadedChunkCompound -> {
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
                Tag blockStatesCompound = dataResult.getOrThrow(false, error -> {
                });

                ListTag sectionsList = unloadedChunkCompound.getSections();
                for (int i = 0; i < sectionsList.size(); ++i)
                    sectionsList.getCompound(i).put("block_states", blockStatesCompound);
            } else {
                ProtoChunk protoChunk = NMSUtils.createProtoChunk(unloadedChunkCompound.getChunkPos(), serverLevel);

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
                            sectionCompound.put("block_states", dataResult.getOrThrow(false, error -> {
                            }));
                        }

                        {
                            DataResult<Tag> dataResult = biomesCodec.encodeStart(NbtOps.INSTANCE, levelChunkSection.getBiomes());
                            sectionCompound.put("biomes", dataResult.getOrThrow(false, error -> {
                            }));
                        }
                    }

                    if (!sectionCompound.isEmpty()) {
                        sectionCompound.putByte("Y", (byte) i);
                        sectionsList.add(sectionCompound);
                    }
                }

                for (BlockPos blockEntityPos : protoChunk.blockEntities.keySet()) {
                    CompoundTag blockEntityCompound = protoChunk.getBlockEntityNbtForSaving(blockEntityPos);
                    if (blockEntityCompound != null)
                        tileEntities.add(blockEntityCompound);
                }

                unloadedChunkCompound.setSections(sectionsList);
            }
        });
    }

    @Override
    public CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions,
                                                                    Map<ChunkPosition, CalculatedChunk> unloadedChunksCache) {
        List<CalculatedChunk> allCalculatedChunks = new LinkedList<>();
        List<ChunkPos> chunksCoords = new LinkedList<>();

        Iterator<ChunkPosition> chunkPositionsIterator = chunkPositions.iterator();
        while (chunkPositionsIterator.hasNext()) {
            ChunkPosition chunkPosition = chunkPositionsIterator.next();
            CalculatedChunk cachedCalculatedChunk = unloadedChunksCache.get(chunkPosition);
            if (cachedCalculatedChunk != null) {
                allCalculatedChunks.add(cachedCalculatedChunk);
                chunkPositionsIterator.remove();
            } else {
                chunksCoords.add(new ChunkPos(chunkPosition.getX(), chunkPosition.getZ()));
            }
        }

        if (chunkPositions.isEmpty())
            return CompletableFuture.completedFuture(allCalculatedChunks);

        CompletableFuture<List<CalculatedChunk>> completableFuture = new CompletableFuture<>();

        ServerLevel serverLevel = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();

        NMSUtils.runActionOnChunks(serverLevel, chunksCoords, false, () -> {
            completableFuture.complete(allCalculatedChunks);
        }, chunk -> {
            ChunkPos chunkPos = chunk.getPos();
            ChunkPosition chunkPosition = ChunkPosition.of(((LevelChunk) chunk).level.getWorld(), chunkPos.x, chunkPos.z);
            allCalculatedChunks.add(calculateChunk(chunkPosition, chunk.getSections()));
        }, unloadedChunkCompound -> {
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
                        blocksDataPalette = dataResult.getOrThrow(false, error -> {
                        });
                    } else {
                        blocksDataPalette = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                                Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                    }

                    PalettedContainer<Holder<Biome>> biomesDataPalette;
                    if (sectionCompound.contains("biomes", 10)) {
                        DataResult<PalettedContainer<Holder<Biome>>> dataResult = biomesCodec.parse(NbtOps.INSTANCE,
                                sectionCompound.getCompound("biomes")).promotePartial((sx) -> {
                        });
                        biomesDataPalette = dataResult.getOrThrow(false, error -> {
                        });
                    } else {
                        biomesDataPalette = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                                biomesRegistry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                    }

                    chunkSections[sectionIndex] = new LevelChunkSection(yPosition, blocksDataPalette, biomesDataPalette);
                }

            }

            ChunkPos chunkPos = unloadedChunkCompound.getChunkPos();
            ChunkPosition chunkPosition = ChunkPosition.of(serverLevel.getWorld(), chunkPos.x, chunkPos.z);
            CalculatedChunk calculatedChunk = calculateChunk(chunkPosition, chunkSections);
            allCalculatedChunks.add(calculatedChunk);
            unloadedChunksCache.put(chunkPosition, calculatedChunk);
        });

        return completableFuture;
    }

    @Override
    public void injectChunkSections(org.bukkit.Chunk chunk) {
        // No implementation
    }

    @Override
    public boolean isChunkEmpty(org.bukkit.Chunk bukkitChunk) {
        LevelChunk levelChunk = ((CraftChunk) bukkitChunk).getHandle();
        return Arrays.stream(levelChunk.getSections()).allMatch(chunkSection ->
                chunkSection == null || chunkSection.hasOnlyAir());
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
        ChunkAccess chunkAccess = serverLevel.getChunkSource().getChunk(chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunkAccess instanceof LevelChunk levelChunk ? levelChunk.getBukkitChunk() : null;
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;

        LevelChunk levelChunk = ((CraftChunk) chunk).getHandle();

        if (stop) {
            CropsBlockEntity cropsBlockEntity = CropsBlockEntity.remove(levelChunk.getPos());
            if (cropsBlockEntity != null)
                cropsBlockEntity.remove();
        } else {
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
        LevelChunk levelChunk = ((CraftChunk) chunk).getHandle();
        List<Location> blockEntities = new LinkedList<>();

        World bukkitWorld = chunk.getWorld();

        levelChunk.getBlockEntities().keySet().forEach(blockPos ->
                blockEntities.add(new Location(bukkitWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ())));

        return blockEntities;
    }

    private static CalculatedChunk calculateChunk(ChunkPosition chunkPosition, LevelChunkSection[] chunkSections) {
        KeyMap<Integer> blockCounts = KeyMapImpl.createHashMap();
        Set<Location> spawnersLocations = new HashSet<>();

        for (LevelChunkSection levelChunkSection : chunkSections) {
            if (levelChunkSection != null) {
                for (BlockPos blockPos : BlockPos.betweenClosed(0, 0, 0, 15, 15, 15)) {
                    BlockState blockState = levelChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    Block block = blockState.getBlock();

                    if (block == Blocks.AIR)
                        continue;

                    Location location = new Location(chunkPosition.getWorld(),
                            (chunkPosition.getX() << 4) + blockPos.getX(),
                            levelChunkSection.bottomBlockY() + blockPos.getY(),
                            (chunkPosition.getZ() << 4) + blockPos.getZ());

                    int blockAmount = 1;

                    if (NMSUtils.isDoubleBlock(block, blockState)) {
                        blockAmount = 2;
                        blockState = blockState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                    }

                    Material type = CraftMagicNumbers.getMaterial(blockState.getBlock());
                    Key blockKey = KeyImpl.of(type.name() + "", "0", location);
                    blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + blockAmount);
                    if (type == Material.SPAWNER) {
                        spawnersLocations.add(location);
                    }
                }
            }
        }

        return new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations);
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
