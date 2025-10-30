package com.bgsoftware.superiorskyblock.nms.v1_21_3;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.collections.Chunk2ObjectMap;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.nms.v1_21_3.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_21_3.utils.NMSUtilsVersioned;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
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
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NMSChunksImpl extends com.bgsoftware.superiorskyblock.nms.v1_21_3.AbstractNMSChunks {

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    protected NMSUtils.ChunkCallback getBiomesChunkCallback(org.bukkit.block.Biome bukkitBiome, Collection<Player> playersToUpdate) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.SET_BIOME, false) {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                Registry<Biome> biomesRegistry = levelChunk.level.registryAccess().lookupOrThrow(Registries.BIOME);
                Holder<Biome> biome = CraftBiome.bukkitToMinecraftHolder(bukkitBiome);

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

                levelChunk.markUnsaved();

                ClientboundForgetLevelChunkPacket forgetLevelChunkPacket = new ClientboundForgetLevelChunkPacket(chunkPos);
                ClientboundLevelChunkWithLightPacket mapChunkPacket = new ClientboundLevelChunkWithLightPacket(
                        levelChunk, levelChunk.level.getLightEngine(), null, null, true);

                playersToUpdate.forEach(player -> {
                    ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                    serverPlayer.connection.send(forgetLevelChunkPacket);
                    serverPlayer.connection.send(mapChunkPacket);
                });
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();

                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();
                Registry<Biome> biomesRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.BIOME);
                Holder<Biome> biome = CraftBiome.bukkitToMinecraftHolder(bukkitBiome);

                Codec<PalettedContainer<Holder<Biome>>> biomesCodec = PalettedContainer.codecRW(
                        biomesRegistry.asHolderIdMap(),
                        biomesRegistry.holderByNameCodec(),
                        PalettedContainer.Strategy.SECTION_BIOMES,
                        biomesRegistry.getOrThrow(Biomes.PLAINS)
                );
                PalettedContainer<Holder<Biome>> biomesContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                        biome, PalettedContainer.Strategy.SECTION_BIOMES);
                DataResult<Tag> dataResult = biomesCodec.encodeStart(NbtOps.INSTANCE, biomesContainer);
                Tag biomesCompound = dataResult.getOrThrow();

                ListTag sectionsList = chunkCompound.getList("sections", 10);

                for (int i = 0; i < sectionsList.size(); ++i)
                    sectionsList.getCompound(i).put("biomes", biomesCompound);
            }

            @Override
            public void onFinish() {
                // Do nothing.
            }
        };
    }

    @Override
    protected NMSUtils.ChunkCallback getDeleteChunkCallback(Runnable onFinish) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.DELETE_CHUNK, false) {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                Registry<Biome> biomesRegistry = levelChunk.level.registryAccess().lookupOrThrow(Registries.BIOME);

                LevelChunkSection[] chunkSections = levelChunk.getSections();
                for (int i = 0; i < chunkSections.length; ++i) {
                    chunkSections[i] = new LevelChunkSection(biomesRegistry);
                }

                removeEntities(levelChunk);
                removeBlockEntities(levelChunk);
                removeBlocks(levelChunk);
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();
                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();

                Codec<PalettedContainer<BlockState>> blocksCodec = PalettedContainer.codecRW(
                        Block.BLOCK_STATE_REGISTRY,
                        BlockState.CODEC,
                        PalettedContainer.Strategy.SECTION_STATES,
                        Blocks.AIR.defaultBlockState());

                ListTag tileEntities = new ListTag();

                chunkCompound.put("Entities", new ListTag());
                chunkCompound.put("block_entities", tileEntities);

                if (serverLevel.generator instanceof IslandsGenerator) {
                    PalettedContainer<BlockState> statesContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                            Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                    DataResult<Tag> dataResult = blocksCodec.encodeStart(NbtOps.INSTANCE, statesContainer);
                    Tag blockStatesCompound = dataResult.getOrThrow();

                    ListTag sectionsList = chunkCompound.getList("sections", 10);
                    for (int i = 0; i < sectionsList.size(); ++i)
                        sectionsList.getCompound(i).put("block_states", blockStatesCompound);
                } else {
                    ProtoChunk protoChunk = NMSUtils.createProtoChunk(unloadedChunkCompound.chunkPos(), serverLevel);

                    try {
                        NMSUtilsVersioned.buildSurfaceForChunk(serverLevel, serverLevel.generator, protoChunk);
                    } catch (Exception ignored) {
                    }

                    Registry<Biome> biomesRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.BIOME);

                    Codec<PalettedContainerRO<Holder<Biome>>> biomesCodec = PalettedContainer.codecRO(
                            biomesRegistry.asHolderIdMap(),
                            biomesRegistry.holderByNameCodec(),
                            PalettedContainer.Strategy.SECTION_BIOMES,
                            biomesRegistry.getOrThrow(Biomes.PLAINS)
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

                    chunkCompound.put("sections", sectionsList);
                }
            }

            @Override
            public void onFinish() {
                if (onFinish != null)
                    onFinish.run();
            }
        };
    }

    @Override
    protected NMSUtils.ChunkCallback getCalculateChunkCallback(CompletableFuture<List<CalculatedChunk.Blocks>> completableFuture,
                                                               Synchronized<Chunk2ObjectMap<CalculatedChunk.Blocks>> unloadedChunksCache,
                                                               List<CalculatedChunk.Blocks> allCalculatedChunks) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.BLOCKS_RECALCULATE, true) {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                ChunkPos chunkPos = levelChunk.getPos();
                ChunkPosition chunkPosition = ChunkPosition.of(levelChunk.level.getWorld(), chunkPos.x, chunkPos.z, false);
                allCalculatedChunks.add(calculateChunk(chunkPosition, levelChunk.level, levelChunk.getSections()));

                latchCountDown();
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                ChunkPosition chunkPosition = unloadedChunkCompound.chunkPosition();
                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();
                Registry<Biome> biomesRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.BIOME);

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
                        biomesRegistry.getOrThrow(Biomes.PLAINS)
                );

                LevelChunkSection[] chunkSections = new LevelChunkSection[serverLevel.getSectionsCount()];

                ListTag sectionsList = chunkCompound.getList("sections", 10);
                for (int i = 0; i < sectionsList.size(); ++i) {
                    CompoundTag sectionCompound = sectionsList.getCompound(i);
                    byte yPosition = sectionCompound.getByte("Y");
                    int sectionIndex = serverLevel.getSectionIndexFromSectionY(yPosition);

                    if (sectionIndex >= 0 && sectionIndex < chunkSections.length) {
                        PalettedContainer<BlockState> blocksPalettedContainer;
                        if (sectionCompound.contains("block_states", 10)) {
                            DataResult<PalettedContainer<BlockState>> dataResult = blocksCodec.parse(NbtOps.INSTANCE,
                                    sectionCompound.getCompound("block_states")).promotePartial((sx) -> {
                            });
                            blocksPalettedContainer = dataResult.getOrThrow();
                        } else {
                            blocksPalettedContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                                    Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                        }

                        PalettedContainer<Holder<Biome>> biomesPalettedContainer;
                        if (sectionCompound.contains("biomes", 10)) {
                            DataResult<PalettedContainer<Holder<Biome>>> dataResult = biomesCodec.parse(NbtOps.INSTANCE,
                                    sectionCompound.getCompound("biomes")).promotePartial((sx) -> {
                            });
                            biomesPalettedContainer = dataResult.getOrThrow();
                        } else {
                            biomesPalettedContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                                    biomesRegistry.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                        }

                        chunkSections[sectionIndex] = new LevelChunkSection(blocksPalettedContainer, biomesPalettedContainer);
                    }

                }

                CalculatedChunk.Blocks calculatedChunk = calculateChunk(chunkPosition, serverLevel, chunkSections);
                allCalculatedChunks.add(calculatedChunk);
                unloadedChunksCache.write(m -> m.put(chunkPosition, calculatedChunk));

                latchCountDown();
            }

            @Override
            public void onFinish() {
                completableFuture.complete(allCalculatedChunks);
            }
        };
    }

    @Override
    protected NMSUtils.ChunkCallback getEntitiesChunkCallback(List<CalculatedChunk.Entities> allCalculatedChunks,
                                                              List<NMSUtils.UnloadedChunkCompound> unloadedChunkCompounds,
                                                              CompletableFuture<List<CalculatedChunk.Entities>> completableFuture) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.ENTITIES_RECALCULATE, true) {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                ChunkPos chunkPos = levelChunk.getPos();
                ChunkPosition chunkPosition = ChunkPosition.of(levelChunk.level.getWorld(), chunkPos.x, chunkPos.z, false);
                allCalculatedChunks.add(calculatedChunk(chunkPosition, levelChunk));

                latchCountDown();
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                unloadedChunkCompounds.add(unloadedChunkCompound);

                latchCountDown();
            }

            @Override
            public void onFinish() {
                BukkitExecutor.ensureMain(() -> {
                    for (NMSUtils.UnloadedChunkCompound unloadedChunkCompound : unloadedChunkCompounds) {
                        ListTag entitiesTag = unloadedChunkCompound.chunkCompound().getList("Entities", 10);
                        allCalculatedChunks.add(calculatedChunk(unloadedChunkCompound.chunkPosition(),
                                unloadedChunkCompound.serverLevel(), entitiesTag));
                    }

                    completableFuture.complete(allCalculatedChunks);
                });
            }
        };
    }

    @Override
    protected Optional<Entity> createEntityFromTag(CompoundTag compoundTag, ServerLevel serverLevel) {
        int dataVersion = compoundTag.getInt("DataVersion");
        if (dataVersion < com.bgsoftware.superiorskyblock.nms.v1_21_3.AbstractNMSAlgorithms.DATA_VERSION) {
            compoundTag = (net.minecraft.nbt.CompoundTag) DataFixers.getDataFixer().update(References.ENTITY_CHUNK,
                    new Dynamic<>(NbtOps.INSTANCE, compoundTag), dataVersion,
                    com.bgsoftware.superiorskyblock.nms.v1_21_3.AbstractNMSAlgorithms.DATA_VERSION).getValue();
        }

        return EntityType.create(compoundTag, serverLevel, EntitySpawnReason.NATURAL);
    }

    private static void removeBlocks(ChunkAccess chunk) {
        ServerLevel serverLevel = ((LevelChunk) chunk).level;
        ChunkGenerator bukkitGenerator = serverLevel.getWorld().getGenerator();

        if (bukkitGenerator == null || bukkitGenerator instanceof IslandsGenerator)
            return;

        NMSUtilsVersioned.buildSurfaceForChunk(serverLevel, bukkitGenerator, chunk);
    }

}
