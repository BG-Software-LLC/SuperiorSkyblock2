package com.bgsoftware.superiorskyblock.nms.v117;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.v117.world.PropertiesMapper;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
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
import net.minecraft.world.level.levelgen.Heightmap;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NMSUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectMethod<Void> SEND_PACKETS_TO_RELEVANT_PLAYERS = new ReflectMethod<>(
            ChunkHolder.class, 1, Packet.class, boolean.class);
    private static final ReflectField<Map<Long, ChunkHolder>> VISIBLE_CHUNKS = new ReflectField<>(
            ChunkMap.class, Map.class, Modifier.PUBLIC | Modifier.VOLATILE, 1);

    private NMSUtils() {

    }

    public static void runActionOnChunks(ServerLevel serverLevel, Collection<ChunkPos> chunksCoords,
                                         boolean saveChunks, Runnable onFinish, Consumer<LevelChunk> chunkConsumer,
                                         BiConsumer<ChunkPos, net.minecraft.nbt.CompoundTag> unloadedChunkConsumer) {
        List<ChunkPos> unloadedChunks = new LinkedList<>();
        List<LevelChunk> loadedChunks = new LinkedList<>();

        chunksCoords.forEach(chunkPos -> {
            ChunkAccess chunkAccess;

            try {
                chunkAccess = serverLevel.getChunkIfLoadedImmediately(chunkPos.x, chunkPos.z);
            } catch (Throwable ex) {
                chunkAccess = serverLevel.getChunkIfLoaded(chunkPos.x, chunkPos.z);
            }

            if (chunkAccess instanceof LevelChunk) {
                loadedChunks.add((LevelChunk) chunkAccess);
            } else {
                unloadedChunks.add(chunkPos);
            }
        });

        boolean hasUnloadedChunks = !unloadedChunks.isEmpty();

        if (!loadedChunks.isEmpty())
            runActionOnLoadedChunks(loadedChunks, chunkConsumer);

        if (hasUnloadedChunks) {
            runActionOnUnloadedChunks(serverLevel, unloadedChunks, saveChunks, unloadedChunkConsumer, onFinish);
        } else if (onFinish != null) {
            onFinish.run();
        }
    }

    public static void runActionOnLoadedChunks(Collection<LevelChunk> chunks, Consumer<LevelChunk> chunkConsumer) {
        chunks.forEach(chunkConsumer);
    }

    public static void runActionOnUnloadedChunks(ServerLevel serverLevel,
                                                 Collection<ChunkPos> chunks,
                                                 boolean saveChunks,
                                                 BiConsumer<ChunkPos, net.minecraft.nbt.CompoundTag> chunkConsumer,
                                                 Runnable onFinish) {
        ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;

        BukkitExecutor.createTask().runAsync(v -> {
            chunks.forEach(chunkCoords -> {
                try {
                    net.minecraft.nbt.CompoundTag chunkCompound = chunkMap.read(chunkCoords);

                    if (chunkCompound == null)
                        return;

                    net.minecraft.nbt.CompoundTag chunkDataCompound = chunkMap.getChunkData(serverLevel.getTypeKey(),
                            Suppliers.ofInstance(serverLevel.getDataStorage()), chunkCompound, chunkCoords, serverLevel);

                    if (chunkDataCompound.contains("Level", 10)) {
                        chunkConsumer.accept(chunkCoords, chunkDataCompound.getCompound("Level"));
                        if (saveChunks)
                            chunkMap.write(chunkCoords, chunkDataCompound);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    PluginDebugger.debug(ex);
                }
            });
        }).runSync(v -> {
            if (onFinish != null)
                onFinish.run();
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

    public static void sendPacketToRelevantPlayers(ServerLevel serverLevel, int chunkX, int chunkZ, Packet<?> packet) {
        ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        ChunkHolder chunkHolder;

        try {
            chunkHolder = chunkMap.getVisibleChunkIfPresent(chunkPos.toLong());
        } catch (Throwable ex) {
            chunkHolder = VISIBLE_CHUNKS.get(chunkMap).get(chunkPos.toLong());
        }

        if (chunkHolder != null) {
            try {
                chunkHolder.broadcast(packet, false);
            } catch (Throwable ex) {
                SEND_PACKETS_TO_RELEVANT_PLAYERS.invoke(chunkHolder, packet, false);
            }
        }
    }

    public static void setBlock(LevelChunk levelChunk, BlockPos blockPos, int combinedId,
                                CompoundTag statesTag, CompoundTag tileEntity) {
        ServerLevel serverLevel = levelChunk.level;

        if (!isValidPosition(serverLevel, blockPos))
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
                } catch (Exception error) {
                    PluginDebugger.debug(error);
                }
            }
        }

        if ((blockState.getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) ||
                blockState.getBlock() instanceof BedBlock) {
            serverLevel.setBlock(blockPos, blockState, 3);
            return;
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

}
