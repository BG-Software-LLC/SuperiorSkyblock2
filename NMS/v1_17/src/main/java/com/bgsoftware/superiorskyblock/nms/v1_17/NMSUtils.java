package com.bgsoftware.superiorskyblock.nms.v1_17;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.v1_17.world.PropertiesMapper;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class NMSUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectMethod<LevelChunk> CHUNK_CACHE_SERVER_GET_CHUNK_IF_CACHED = new ReflectMethod<>(
            ServerChunkCache.class, "getChunkAtIfCachedImmediately", int.class, int.class);
    private static final ReflectField<IOWorker> ENTITY_STORAGE_WORKER = new ReflectField<>(
            EntityStorage.class, IOWorker.class, Modifier.PRIVATE | Modifier.FINAL, 1);
    private static final ReflectMethod<CompletableFuture<net.minecraft.nbt.CompoundTag>> WORKER_LOAD_ASYNC = new ReflectMethod<>(
            IOWorker.class, CompletableFuture.class, 1, ChunkPos.class);

    private static final List<CompletableFuture<Void>> PENDING_CHUNK_ACTIONS = new LinkedList<>();

    public static final ObjectsPool<ObjectsPools.Wrapper<BlockPos.MutableBlockPos>> BLOCK_POS_POOL =
            ObjectsPools.createNewPool(() -> new BlockPos.MutableBlockPos(0, 0, 0));

    private NMSUtils() {

    }

    @Nullable
    public static <T extends BlockEntity> T getBlockEntityAt(Location location, Class<T> type) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return null;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();

        try (ObjectsPools.Wrapper<BlockPos.MutableBlockPos> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPos.MutableBlockPos blockPos = wrapper.getHandle();
            blockPos.set(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            return !type.isInstance(blockEntity) ? null : type.cast(blockEntity);
        }
    }

    public static void runActionOnEntityChunks(Collection<ChunkPosition> chunksCoords,
                                               ChunkCallback chunkCallback) {
        runActionOnChunksInternal(chunksCoords, chunkCallback, unloadedChunks ->
                runActionOnUnloadedEntityChunks(unloadedChunks, chunkCallback));
    }

    public static void runActionOnChunks(Collection<ChunkPosition> chunksCoords,
                                         boolean saveChunks, ChunkCallback chunkCallback) {
        runActionOnChunksInternal(chunksCoords, chunkCallback, unloadedChunks ->
                runActionOnUnloadedChunks(unloadedChunks, saveChunks, chunkCallback));
    }

    private static void runActionOnChunksInternal(Collection<ChunkPosition> chunksCoords,
                                                  ChunkCallback chunkCallback,
                                                  Consumer<List<ChunkPosition>> onUnloadChunkAction) {
        List<ChunkPosition> unloadedChunks = new LinkedList<>();
        List<LevelChunk> loadedChunks = new LinkedList<>();

        chunksCoords.forEach(chunkPosition -> {
            ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();

            ChunkAccess chunkAccess;

            try {
                chunkAccess = serverLevel.getChunkIfLoadedImmediately(chunkPosition.getX(), chunkPosition.getZ());
            } catch (Throwable ex) {
                chunkAccess = serverLevel.getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());
            }

            if (chunkAccess instanceof LevelChunk levelChunk) {
                loadedChunks.add(levelChunk);
            } else {
                unloadedChunks.add(chunkPosition.copy());
            }
        });

        boolean hasUnloadedChunks = !unloadedChunks.isEmpty();

        if (!loadedChunks.isEmpty())
            runActionOnLoadedChunks(loadedChunks, chunkCallback);

        if (hasUnloadedChunks) {
            onUnloadChunkAction.accept(unloadedChunks);
        } else {
            chunkCallback.onFinish();
        }
    }

    private static void runActionOnLoadedChunks(Collection<LevelChunk> chunks, ChunkCallback chunkCallback) {
        chunks.forEach(chunkCallback::onLoadedChunk);
    }

    private static void runActionOnUnloadedChunks(Collection<ChunkPosition> chunks,
                                                  boolean saveChunks, ChunkCallback chunkCallback) {
        if (CHUNK_CACHE_SERVER_GET_CHUNK_IF_CACHED.isValid()) {
            Iterator<ChunkPosition> chunksIterator = chunks.iterator();
            while (chunksIterator.hasNext()) {
                ChunkPosition chunkPosition = chunksIterator.next();

                ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();

                LevelChunk cachedUnloadedChunk = serverLevel.getChunkSource().getChunkAtIfCachedImmediately(
                        chunkPosition.getX(), chunkPosition.getZ());
                if (cachedUnloadedChunk != null) {
                    chunkCallback.onLoadedChunk(cachedUnloadedChunk);
                    chunksIterator.remove();
                }
            }

            if (chunks.isEmpty()) {
                chunkCallback.onFinish();
                return;
            }
        }

        CompletableFuture<Void> pendingTask = new CompletableFuture<>();
        PENDING_CHUNK_ACTIONS.add(pendingTask);

        BukkitExecutor.createTask().runAsync(v -> {
            CountDownLatch countDownLatch;
            if (chunkCallback.isWaitForChunkLoad) {
                countDownLatch = chunkCallback.chunkLoadLatch = new CountDownLatch(chunks.size());
            } else {
                countDownLatch = null;
            }

            chunks.forEach(chunkPosition -> {
                ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;

                ChunkPos chunkPos = new ChunkPos(chunkPosition.getX(), chunkPosition.getZ());

                boolean calledCountdown = false;
                try {
                    net.minecraft.nbt.CompoundTag chunkCompound = chunkMap.read(chunkPos);

                    if (chunkCompound == null) {
                        chunkCallback.onChunkNotExist(chunkPosition);
                        return;
                    }

                    net.minecraft.nbt.CompoundTag chunkDataCompound = chunkMap.getChunkData(serverLevel.getTypeKey(),
                            Suppliers.ofInstance(serverLevel.getDataStorage()), chunkCompound, chunkPos, serverLevel);

                    if (chunkDataCompound.contains("Level", 10)) {
                        chunkCallback.onUnloadedChunk(chunkPosition, chunkDataCompound.getCompound("Level"));
                        calledCountdown = true;
                        if (saveChunks)
                            chunkMap.write(chunkPos, chunkDataCompound);
                    } else {
                        if (countDownLatch != null)
                            countDownLatch.countDown();
                    }
                } catch (Exception error) {
                    if (!calledCountdown && countDownLatch != null)
                        countDownLatch.countDown();
                    Log.error(error, "An unexpected error occurred while interacting with unloaded chunk ", chunkPosition, ":");
                }
            });

            if (countDownLatch != null) {
                try {
                    countDownLatch.await();
                } catch (InterruptedException error) {
                    throw new RuntimeException(error);
                }
            }
        }).runSync(v -> {
            chunkCallback.onFinish();

            pendingTask.complete(null);
            PENDING_CHUNK_ACTIONS.remove(pendingTask);
        });
    }

    private static void runActionOnUnloadedEntityChunks(Collection<ChunkPosition> chunks,
                                                        ChunkCallback chunkCallback) {
        CountDownLatch countDownLatch = chunkCallback.chunkLoadLatch = new CountDownLatch(chunks.size());

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
                        chunkCallback.onUnloadedChunk(chunkPosition, entityData);
                    }
                }
            });
        });

        BukkitExecutor.createTask().runAsync(v -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException error) {
                throw new RuntimeException(error);
            }
        }).runSync(v -> {
            chunkCallback.onFinish();
        });
    }

    public static List<CompletableFuture<Void>> getPendingChunkActions() {
        return Collections.unmodifiableList(PENDING_CHUNK_ACTIONS);
    }

    public static ProtoChunk createProtoChunk(ChunkPos chunkPos, ServerLevel serverLevel) {
        try {
            return new ProtoChunk(chunkPos, UpgradeData.EMPTY, serverLevel, serverLevel);
        } catch (Throwable ex) {
            //noinspection deprecation
            return new ProtoChunk(chunkPos, UpgradeData.EMPTY, serverLevel);
        }
    }

    public static BlockState setBlock(LevelChunk levelChunk, BlockPos blockPos, int combinedId,
                                      CompoundTag statesTag, CompoundTag tileEntity) {
        ServerLevel serverLevel = levelChunk.level;

        if (!isValidPosition(serverLevel, blockPos))
            return null;

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
            serverLevel.setBlock(blockPos, blockState, 3);
            return blockState;
        }

        int indexY = serverLevel.getSectionIndex(blockPos.getY());

        LevelChunkSection levelChunkSection = levelChunk.getSections()[indexY];

        if (levelChunkSection == LevelChunk.EMPTY_SECTION) {
            int yOffset = SectionPos.blockToSectionCoord(blockPos.getY());
            try {
                // Paper's constructor for ChunkSection for more optimized chunk sections.
                levelChunkSection = levelChunk.getSections()[indexY] = new LevelChunkSection(yOffset, levelChunk, serverLevel, true);
            } catch (Throwable ex) {
                // Spigot's constructor for ChunkSection
                // noinspection deprecation
                levelChunkSection = levelChunk.getSections()[indexY] = new LevelChunkSection(yOffset);
            }
        }

        int blockX = blockPos.getX() & 15;
        int blockY = blockPos.getY();
        int blockZ = blockPos.getZ() & 15;

        boolean isOriginallyChunkSectionEmpty = levelChunkSection.isEmpty();

        levelChunkSection.setBlockState(blockX, blockY & 15, blockZ, blockState, false);

        levelChunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(blockX, blockY, blockZ, blockState);
        levelChunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(blockX, blockY, blockZ, blockState);
        levelChunk.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(blockX, blockY, blockZ, blockState);
        levelChunk.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(blockX, blockY, blockZ, blockState);

        levelChunk.setUnsaved(true);

        boolean isChunkSectionEmpty = levelChunkSection.isEmpty();

        if (isOriginallyChunkSectionEmpty != isChunkSectionEmpty)
            serverLevel.getLightEngine().updateSectionStatus(blockPos, isChunkSectionEmpty);

        serverLevel.getLightEngine().checkBlock(blockPos);

        if (tileEntity != null) {
            net.minecraft.nbt.CompoundTag tileEntityCompound = (net.minecraft.nbt.CompoundTag) tileEntity.toNBT();
            if (tileEntityCompound != null) {
                tileEntityCompound.putInt("x", blockPos.getX());
                tileEntityCompound.putInt("y", blockPos.getY());
                tileEntityCompound.putInt("z", blockPos.getZ());
                BlockEntity worldBlockEntity = serverLevel.getBlockEntity(blockPos);
                if (worldBlockEntity != null)
                    worldBlockEntity.load(tileEntityCompound);
            }
        }

        return blockState;
    }

    public static boolean isDoubleBlock(Block block, BlockState blockState) {
        return (block.defaultBlockState().is(BlockTags.SLABS) || block.defaultBlockState().is(BlockTags.WOODEN_SLABS)) &&
                blockState.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
    }

    private static boolean isValidPosition(ServerLevel serverLevel, BlockPos blockPos) {
        return blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 &&
                blockPos.getX() < 30000000 && blockPos.getZ() < 30000000 &&
                blockPos.getY() >= serverLevel.getMinBuildHeight() && blockPos.getY() < serverLevel.getMaxBuildHeight();
    }

    public static abstract class ChunkCallback {

        private final ChunkLoadReason chunkLoadReason;
        private final boolean isWaitForChunkLoad;

        protected CountDownLatch chunkLoadLatch;

        public ChunkCallback(ChunkLoadReason chunkLoadReason, boolean isWaitForChunkLoad) {
            this.chunkLoadReason = chunkLoadReason;
            this.isWaitForChunkLoad = isWaitForChunkLoad;
        }

        public abstract void onLoadedChunk(LevelChunk levelChunk);

        public abstract void onUnloadedChunk(ChunkPosition chunkPosition, net.minecraft.nbt.CompoundTag unloadedChunk);

        public abstract void onFinish();

        public final void onChunkNotExist(ChunkPosition chunkPosition) {
            if (!plugin.getProviders().hasCustomWorldsSupport())
                return;

            ChunksProvider.loadChunk(chunkPosition, this.chunkLoadReason, bukkitChunk -> {
                LevelChunk levelChunk = ((CraftChunk) bukkitChunk).getHandle();
                BukkitExecutor.ensureMain(() -> onLoadedChunk(levelChunk));
            });
        }

    }

}
