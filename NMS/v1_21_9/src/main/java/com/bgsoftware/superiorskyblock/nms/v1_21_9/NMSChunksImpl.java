package com.bgsoftware.superiorskyblock.nms.v1_21_9;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.collections.Chunk2ObjectMap;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.nms.v1_21_9.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_21_9.utils.NMSUtilsVersioned;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.bgsoftware.superiorskyblock.nms.v1_21_9.utils.NMSUtilsVersioned.DEFAULT_PALETTED_CONTAINER_FACTORY;

public class NMSChunksImpl extends com.bgsoftware.superiorskyblock.nms.v1_21_9.AbstractNMSChunks {

    private static final ReflectMethod<Codec<PalettedContainer<Holder<Biome>>>> CONTAINER_FACTORY_BIOME_RW_CODEC =
            new ReflectMethod<>(PalettedContainerFactory.class, "biomeContainerCodecRW");

    private static final Logger LOGGER = LogUtils.getLogger();

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    protected NMSUtils.ChunkCallback getBiomesChunkCallback(org.bukkit.block.Biome bukkitBiome, Collection<Player> playersToUpdate) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.SET_BIOME, false) {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                Holder<Biome> biome = CraftBiome.bukkitToMinecraftHolder(bukkitBiome);

                ChunkPos chunkPos = levelChunk.getPos();

                LevelChunkSection[] chunkSections = levelChunk.getSections();
                for (int i = 0; i < chunkSections.length; ++i) {
                    LevelChunkSection currentSection = chunkSections[i];
                    if (currentSection != null) {
                        PalettedContainer<Holder<Biome>> biomesContainer = NMSUtilsVersioned.createBiomesContainer(biome);
                        chunkSections[i] = new LevelChunkSection(currentSection.getStates(), biomesContainer);
                    }
                }

                levelChunk.markUnsaved();

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
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();

                Holder<Biome> biome = CraftBiome.bukkitToMinecraftHolder(bukkitBiome);

                PalettedContainer<Holder<Biome>> biomesContainer = NMSUtilsVersioned.createBiomesContainer(biome);
                DataResult<Tag> dataResult = getBiomeContainerRWCodec()
                        .encodeStart(NbtOps.INSTANCE, biomesContainer);
                Tag biomesCompound = dataResult.getOrThrow();

                ListTag sectionsList = chunkCompound.getListOrEmpty("sections");

                for (int i = 0; i < sectionsList.size(); ++i)
                    sectionsList.getCompound(i).ifPresent(compound -> compound.put("biomes", biomesCompound));
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
                LevelChunkSection[] chunkSections = levelChunk.getSections();
                for (int i = 0; i < chunkSections.length; ++i) {
                    chunkSections[i] = new LevelChunkSection(DEFAULT_PALETTED_CONTAINER_FACTORY);
                }

                removeEntities(levelChunk);
                removeBlockEntities(levelChunk);
                removeBlocks(levelChunk);
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();
                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();

                ListTag tileEntities = new ListTag();

                chunkCompound.put("entities", new ListTag());
                chunkCompound.put("block_entities", tileEntities);

                if (serverLevel.generator instanceof IslandsGenerator) {
                    PalettedContainer<BlockState> statesContainer = DEFAULT_PALETTED_CONTAINER_FACTORY.createForBlockStates();
                    DataResult<Tag> dataResult = DEFAULT_PALETTED_CONTAINER_FACTORY.blockStatesContainerCodec()
                            .encodeStart(NbtOps.INSTANCE, statesContainer);
                    Tag blockStatesCompound = dataResult.getOrThrow();

                    ListTag sectionsList = chunkCompound.getListOrEmpty("sections");
                    for (int i = 0; i < sectionsList.size(); ++i)
                        sectionsList.getCompound(i).ifPresent(compound -> compound.put("block_states", blockStatesCompound));
                } else {
                    ProtoChunk protoChunk = NMSUtils.createProtoChunk(unloadedChunkCompound.chunkPos(), serverLevel);

                    try {
                        NMSUtilsVersioned.buildSurfaceForChunk(serverLevel, serverLevel.generator, protoChunk);
                    } catch (Exception ignored) {
                    }

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
                                DataResult<Tag> dataResult = DEFAULT_PALETTED_CONTAINER_FACTORY.blockStatesContainerCodec()
                                        .encodeStart(NbtOps.INSTANCE, levelChunkSection.getStates());
                                sectionCompound.put("block_states", dataResult.getOrThrow());
                            }

                            {
                                DataResult<Tag> dataResult = DEFAULT_PALETTED_CONTAINER_FACTORY.biomeContainerCodec()
                                        .encodeStart(NbtOps.INSTANCE, levelChunkSection.getBiomes());
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

                LevelChunkSection[] chunkSections = new LevelChunkSection[serverLevel.getSectionsCount()];

                ListTag sectionsList = chunkCompound.getListOrEmpty("sections");
                for (int i = 0; i < sectionsList.size(); ++i) {
                    CompoundTag sectionCompound = sectionsList.getCompoundOrEmpty(i);
                    byte yPosition = sectionCompound.getByteOr("Y", (byte) 0);
                    int sectionIndex = serverLevel.getSectionIndexFromSectionY(yPosition);

                    if (sectionIndex >= 0 && sectionIndex < chunkSections.length) {
                        PalettedContainer<BlockState> blocksPalettedContainer;
                        Optional<CompoundTag> blockStatesCompound = sectionCompound.getCompound("block_states");
                        if (blockStatesCompound.isPresent()) {
                            DataResult<PalettedContainer<BlockState>> dataResult = DEFAULT_PALETTED_CONTAINER_FACTORY
                                    .blockStatesContainerCodec().parse(NbtOps.INSTANCE, blockStatesCompound.get())
                                    .promotePartial((sx) -> {
                                    });
                            blocksPalettedContainer = dataResult.getOrThrow();
                        } else {
                            blocksPalettedContainer = DEFAULT_PALETTED_CONTAINER_FACTORY.createForBlockStates();
                        }

                        PalettedContainer<Holder<Biome>> biomesPalettedContainer;
                        Optional<CompoundTag> biomesCompound = sectionCompound.getCompound("biomes");
                        if (biomesCompound.isPresent()) {
                            DataResult<PalettedContainer<Holder<Biome>>> dataResult = getBiomeContainerRWCodec()
                                    .parse(NbtOps.INSTANCE, biomesCompound.get())
                                    .promotePartial((sx) -> {
                                    });
                            biomesPalettedContainer = dataResult.getOrThrow();
                        } else {
                            biomesPalettedContainer = DEFAULT_PALETTED_CONTAINER_FACTORY.createForBiomes();
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
                        ListTag entitiesTag = unloadedChunkCompound.chunkCompound().getListOrEmpty("entities");
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
        try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(compoundTag::toString, LOGGER)) {
            ValueInput valueInput = TagValueInput.create(scopedCollector, serverLevel.registryAccess(), compoundTag);
            return EntityType.create(valueInput, serverLevel, EntitySpawnReason.NATURAL);
        }
    }

    private static void removeBlocks(ChunkAccess chunk) {
        ServerLevel serverLevel = ((LevelChunk) chunk).level;
        ChunkGenerator bukkitGenerator = serverLevel.getWorld().getGenerator();

        if (bukkitGenerator == null || bukkitGenerator instanceof IslandsGenerator)
            return;

        NMSUtilsVersioned.buildSurfaceForChunk(serverLevel, bukkitGenerator, chunk);
    }

    private static Codec<PalettedContainer<Holder<Biome>>> getBiomeContainerRWCodec() {
        if (CONTAINER_FACTORY_BIOME_RW_CODEC.isValid()) {
            return CONTAINER_FACTORY_BIOME_RW_CODEC.invoke(DEFAULT_PALETTED_CONTAINER_FACTORY);
        } else {
            return DEFAULT_PALETTED_CONTAINER_FACTORY.biomeContainerRWCodec();
        }
    }

}
