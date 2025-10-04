package com.bgsoftware.superiorskyblock.nms.v1_19.utils;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.nms.v1_19.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_19.utils.TickingBlockList;
import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class NMSUtilsVersioned {

    private static final ReflectField<PersistentEntitySectionManager<Entity>> SERVER_LEVEL_ENTITY_MANAGER = new ReflectField<>(
            ServerLevel.class, PersistentEntitySectionManager.class, Modifier.PUBLIC | Modifier.FINAL, 1);
    private static final ReflectField<IOWorker> ENTITY_STORAGE_WORKER = new ReflectField<>(
            EntityStorage.class, IOWorker.class, Modifier.PRIVATE | Modifier.FINAL, 1);

    public static CompoundTag readChunk(ChunkMap chunkMap, ChunkPos chunkPos) {
        return chunkMap.read(chunkPos).join().orElse(null);
    }

    public static CompoundTag getChunkData(ChunkMap chunkMap, CompoundTag chunkCompoundTag, ServerLevel serverLevel, ChunkPos chunkPos) {
        return chunkMap.upgradeChunkTag(serverLevel.getTypeKey(), Suppliers.ofInstance(serverLevel.getDataStorage()),
                chunkCompoundTag, Optional.empty(), chunkPos, serverLevel);
    }

    public static void saveChunkData(ChunkMap chunkMap, CompoundTag chunkCompoundTag, ChunkPos chunkPos) throws IOException {
        chunkMap.write(chunkPos, chunkCompoundTag);
    }

    public static BukkitExecutor.NestedTask<Void> runActionOnUnloadedEntityChunks(
            Collection<ChunkPosition> chunks, NMSUtils.ChunkCallback chunkCallback, CountDownLatch countDownLatch) {
        if (SERVER_LEVEL_ENTITY_MANAGER.isValid()) {
            return BukkitExecutor.createTask().runSync(v -> {
                chunks.forEach(chunkPosition -> {
                    ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                    PersistentEntitySectionManager<Entity> entityManager = SERVER_LEVEL_ENTITY_MANAGER.get(serverLevel);
                    IOWorker worker = ENTITY_STORAGE_WORKER.get(entityManager.permanentStorage);

                    ChunkPos chunkPos = new ChunkPos(chunkPosition.getX(), chunkPosition.getZ());

                    worker.loadAsync(chunkPos).whenComplete((entityDataOptional, error) -> {
                        if (error != null) {
                            countDownLatch.countDown();
                            throw new RuntimeException(error);
                        } else {
                            net.minecraft.nbt.CompoundTag entityData = entityDataOptional.orElse(null);
                            if (entityData == null) {
                                chunkCallback.onChunkNotExist(chunkPosition);
                            } else {
                                NMSUtils.UnloadedChunkCompound unloadedChunkCompound = new NMSUtils.UnloadedChunkCompound(chunkPosition, entityData);
                                chunkCallback.onUnloadedChunk(unloadedChunkCompound);
                            }
                        }
                    });
                });
            });
        } else {
            return BukkitExecutor.createTask().runAsync(v -> {
                chunks.forEach(chunkPosition -> {
                    ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();

                    try {
                        CompoundTag entityData = serverLevel.entityDataControllerNew.readData(
                                chunkPosition.getX(), chunkPosition.getZ());

                        if (entityData == null) {
                            chunkCallback.onChunkNotExist(chunkPosition);
                            return;
                        }

                        NMSUtils.UnloadedChunkCompound unloadedChunkCompound = new NMSUtils.UnloadedChunkCompound(chunkPosition, entityData);
                        chunkCallback.onUnloadedChunk(unloadedChunkCompound);
                    } catch (IOException error) {
                        countDownLatch.countDown();
                        Log.error(error, "An unexpected error occurred while interacting with unloaded chunk ", chunkPosition, ":");
                    }
                });
            });
        }
    }

    public static ProtoChunk createProtoChunk(ChunkPos chunkPos, ServerLevel serverLevel) {
        return new ProtoChunk(chunkPos,
                UpgradeData.EMPTY,
                serverLevel,
                serverLevel.registryAccess().registryOrThrow(Registries.BIOME),
                null);
    }

    public static ProtoChunk createProtoChunk(ChunkPos chunkPos, LevelChunkSection[] chunkSections,
                                              LevelHeightAccessor levelHeightAccessor, @Nullable ServerLevel serverLevel) {
        Registry<Biome> biomesRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        return new ProtoChunk(chunkPos, UpgradeData.EMPTY, chunkSections,
                new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), levelHeightAccessor, biomesRegistry, null);
    }

    public static boolean isBlockStateLiquid(BlockState blockState) {
        return blockState.getMaterial().isLiquid();
    }

    public static boolean isLevelChunkSectionEmpty(LevelChunkSection levelChunkSection) {
        return levelChunkSection.hasOnlyAir();
    }

    public static void loadBlockEntity(BlockEntity blockEntity, CompoundTag compoundTag) {
        blockEntity.load(compoundTag);
    }

    public static void relightChunks(ThreadedLevelLightEngine lightEngine, Set<ChunkPos> chunks) {
        lightEngine.relight(chunks, chunkCallback -> {
        }, completeCallback -> {
        });
    }

    public static void rewriteSignLines(CompoundTag compoundTag) {
        if (compoundTag.getByte("SSB.HasSignLines") == 1) {
            // We want to convert the sign lines from raw string to json
            for (int i = 1; i <= 4; ++i) {
                String line = compoundTag.getString("SSB.Text" + i);
                if (!Text.isBlank(line)) {
                    Component newLine = CraftChatMessage.fromString(line)[0];
                    compoundTag.putString("Text" + i, Component.Serializer.toJson(newLine));
                }
            }
        }
    }

    public static DimensionType getDimensionTypeFromDimension(Dimension dimension) {
        ResourceKey<LevelStem> resourceKey;
        switch (dimension.getEnvironment()) {
            case NETHER -> resourceKey = LevelStem.NETHER;
            case THE_END -> resourceKey = LevelStem.END;
            default -> resourceKey = LevelStem.OVERWORLD;
        }
        HolderLookup.RegistryLookup<LevelStem> registry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.LEVEL_STEM);
        return registry.getOrThrow(resourceKey).value().type().value();
    }

    public static void createChunkSections(@Nullable ServerLevel serverLevel,
                                           LevelHeightAccessor levelHeightAccessor,
                                           LevelChunkSection[] chunkSections,
                                           Dimension dimension) {
        Registry<Biome> biomesRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);

        Holder<Biome> biome = CraftBlock.biomeToBiomeBase(biomesRegistry,
                IslandUtils.getDefaultWorldBiome(dimension));

        for (int i = 0; i < chunkSections.length; ++i) {
            int chunkSectionPos = levelHeightAccessor.getSectionYFromSectionIndex(i);

            PalettedContainer<Holder<Biome>> biomesContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                    biome, PalettedContainer.Strategy.SECTION_BIOMES);
            PalettedContainer<BlockState> statesContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                    Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);

            chunkSections[i] = new LevelChunkSection(chunkSectionPos, statesContainer, biomesContainer);
        }
    }

    public static void buildSurfaceForChunk(ServerLevel serverLevel, ChunkGenerator bukkitGenerator, ChunkAccess chunkAccess) {
        CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(serverLevel,
                serverLevel.getChunkSource().getGenerator(), bukkitGenerator);

        WorldGenRegion region = new WorldGenRegion(serverLevel, Collections.singletonList(chunkAccess),
                ChunkStatus.SURFACE, 0);

        chunkGenerator.buildSurface(region,
                serverLevel.structureManager().forWorldGenRegion(region),
                serverLevel.getChunkSource().randomState(),
                chunkAccess);
    }

    public static PalettedContainer<BlockState> createEmptyPlattedContainerStates() {
        return new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(),
                PalettedContainer.Strategy.SECTION_STATES);
    }

    public static PalettedContainer<BlockState> copyPalettedContainer(PalettedContainer<BlockState> original) {
        return original.copy();
    }

    public static CompoundTag saveBlockEntity(BlockEntity blockEntity) {
        return blockEntity.saveWithFullMetadata();
    }

    public static CompoundTag saveEntity(Entity entity) {
        CompoundTag compoundTag = new CompoundTag();
        entity.save(compoundTag);
        return compoundTag;
    }

    public static Material getMaterialFromBlock(Block block) {
        return CraftMagicNumbers.getMaterial(block);
    }

    public static ServerPlayer createServerPlayer(ServerLevel serverLevel, GameProfile gameProfile) {
        return new ServerPlayer(MinecraftServer.getServer(), serverLevel, gameProfile);
    }

    public static TickingBlockList getTickingBlockList(LevelChunkSection levelChunkSection) {
        return new TickingBlockList() {
            @Override
            public int size() {
                return levelChunkSection.tickingList.size();
            }

            @Override
            public int getRaw(int index) {
                return (int) levelChunkSection.tickingList.getRaw(index);
            }
        };
    }

    public static void addEntity(ServerLevel serverLevel, Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        serverLevel.addFreshEntity(entity, spawnReason);
    }

    public static String getPropertyValue(Property property) {
        return property.getValue();
    }

    public static EndDragonFight getEndDragonFight(ServerLevel serverLevel) {
        return serverLevel.dragonFight();
    }

    public static void moveEntity(Entity entity, double x, double y, double z, float yaw, float pitch) {
        entity.absMoveTo(x, y, z, yaw, pitch);
    }

    public static int getMinSection(ServerLevel serverLevel) {
        return serverLevel.getMinSection();
    }

    public static int getMinBuildHeight(ServerLevel serverLevel) {
        return serverLevel.getMinBuildHeight();
    }

    public static int getMaxBuildHeight(ServerLevel serverLevel) {
        return serverLevel.getMaxBuildHeight();
    }

    public static void markUnsaved(LevelChunk levelChunk) {
        levelChunk.setUnsaved(true);
    }

    private NMSUtilsVersioned() {

    }

}
