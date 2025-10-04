package com.bgsoftware.superiorskyblock.nms.v1_17.utils;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.nms.v1_17.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_17.utils.TickingBlockList;
import com.bgsoftware.superiorskyblock.nms.v1_17.world.WorldEditSessionImpl;
import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class NMSUtilsVersioned {

    private static final ReflectField<IOWorker> ENTITY_STORAGE_WORKER = new ReflectField<>(
            EntityStorage.class, IOWorker.class, Modifier.PRIVATE | Modifier.FINAL, 1);
    private static final ReflectMethod<CompletableFuture<CompoundTag>> WORKER_LOAD_ASYNC = new ReflectMethod<>(
            IOWorker.class, CompletableFuture.class, 1, ChunkPos.class);

    public static CompoundTag readChunk(ChunkMap chunkMap, ChunkPos chunkPos) throws IOException {
        return chunkMap.read(chunkPos);
    }

    public static CompoundTag getChunkData(ChunkMap chunkMap, CompoundTag chunkCompoundTag, ServerLevel serverLevel, ChunkPos chunkPos) throws IOException {
        return chunkMap.getChunkData(serverLevel.getTypeKey(), Suppliers.ofInstance(serverLevel.getDataStorage()),
                chunkCompoundTag, chunkPos, serverLevel);
    }

    public static void saveChunkData(ChunkMap chunkMap, CompoundTag chunkCompoundTag, ChunkPos chunkPos) throws IOException {
        chunkMap.write(chunkPos, chunkCompoundTag);
    }

    public static BukkitExecutor.NestedTask<Void> runActionOnUnloadedEntityChunks(
            Collection<ChunkPosition> chunks, NMSUtils.ChunkCallback chunkCallback, CountDownLatch countDownLatch) {
        return BukkitExecutor.createTask().runSync(v -> {
            chunks.forEach(chunkPosition -> {
                ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                IOWorker worker = ENTITY_STORAGE_WORKER.get(serverLevel.entityManager.permanentStorage);

                ChunkPos chunkPos = new ChunkPos(chunkPosition.getX(), chunkPosition.getZ());

                WORKER_LOAD_ASYNC.invoke(worker, chunkPos).whenComplete((entityData, error) -> {
                    if (error != null) {
                        countDownLatch.countDown();
                        throw new RuntimeException(error);
                    } else {
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
    }

    public static ProtoChunk createProtoChunk(ChunkPos chunkPos, ServerLevel serverLevel) {
        try {
            return new ProtoChunk(chunkPos, UpgradeData.EMPTY, serverLevel, serverLevel);
        } catch (Throwable ex) {
            //noinspection deprecation
            return new ProtoChunk(chunkPos, UpgradeData.EMPTY, serverLevel);
        }
    }

    public static ProtoChunk createProtoChunk(ChunkPos chunkPos, LevelChunkSection[] chunkSections,
                                              LevelHeightAccessor levelHeightAccessor, @Nullable ServerLevel serverLevel) {
        ProtoTickList<Block> blockTickScheduler = new ProtoTickList<>(block -> {
            return block == null || block.defaultBlockState().isAir();
        }, chunkPos, levelHeightAccessor);
        ProtoTickList<Fluid> fluidTickScheduler = new ProtoTickList<>((fluid) -> {
            return fluid == null || fluid == Fluids.EMPTY;
        }, chunkPos, levelHeightAccessor);

        try {
            return new ProtoChunk(chunkPos, UpgradeData.EMPTY, chunkSections, blockTickScheduler, fluidTickScheduler, levelHeightAccessor, serverLevel);
        } catch (Throwable error) {
            return new ProtoChunk(chunkPos, UpgradeData.EMPTY, chunkSections, blockTickScheduler, fluidTickScheduler, levelHeightAccessor);
        }
    }

    public static boolean isBlockStateLiquid(BlockState blockState) {
        return blockState.getMaterial().isLiquid();
    }

    public static boolean isLevelChunkSectionEmpty(LevelChunkSection levelChunkSection) {
        return levelChunkSection.isEmpty();
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
        ResourceKey<DimensionType> resourceKey;
        switch (dimension.getEnvironment()) {
            case NETHER -> resourceKey = DimensionType.NETHER_LOCATION;
            case THE_END -> resourceKey = DimensionType.END_LOCATION;
            default -> resourceKey = DimensionType.OVERWORLD_LOCATION;
        }

        Registry<DimensionType> registry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        return registry.getOrThrow(resourceKey);
    }

    public static void createChunkSections(@Nullable ServerLevel serverLevel,
                                           LevelHeightAccessor levelHeightAccessor,
                                           LevelChunkSection[] chunkSections,
                                           Dimension dimension) {
        for (int i = 0; i < chunkSections.length; ++i) {
            int chunkSectionPos = levelHeightAccessor.getSectionYFromSectionIndex(i);

            try {
                chunkSections[i] = new LevelChunkSection(chunkSectionPos, null, serverLevel, true);
            } catch (Throwable error) {
                chunkSections[i] = new LevelChunkSection(chunkSectionPos);
            }
        }
    }

    public static void buildSurfaceForChunk(ServerLevel serverLevel, ChunkGenerator bukkitGenerator, ProtoChunk protoChunk) {
        CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(serverLevel,
                serverLevel.getChunkSource().getGenerator(), bukkitGenerator);

        WorldGenRegion region = new WorldGenRegion(serverLevel, Collections.singletonList(protoChunk),
                ChunkStatus.SURFACE, 0);

        chunkGenerator.buildSurface(region, protoChunk);
    }

    public static PalettedContainer<BlockState> createEmptyPlattedContainerStates() {
        return new LevelChunkSection(0).getStates();
    }

    public static PalettedContainer<BlockState> copyPalettedContainer(PalettedContainer<BlockState> original) {
        CompoundTag data = new CompoundTag();
        original.write(data, "Palette", "BlockStates");

        PalettedContainer<BlockState> blockids = new PalettedContainer<>(LevelChunkSection.GLOBAL_BLOCKSTATE_PALETTE,
                Block.BLOCK_STATE_REGISTRY, NbtUtils::readBlockState, NbtUtils::writeBlockState,
                Blocks.AIR.defaultBlockState(), null, false);
        blockids.read(data.getList("Palette", CraftMagicNumbers.NBT.TAG_COMPOUND), data.getLongArray("BlockStates"));

        return blockids;
    }

    public static CompoundTag saveBlockEntity(BlockEntity blockEntity) {
        return blockEntity.save(new CompoundTag());
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
        serverLevel.addEntity(entity, spawnReason);
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
        levelChunk.markUnsaved();
    }

    private NMSUtilsVersioned() {

    }

}
