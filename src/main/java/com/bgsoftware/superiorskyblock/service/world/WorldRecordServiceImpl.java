package com.bgsoftware.superiorskyblock.service.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.service.records.RecordResult;
import com.bgsoftware.superiorskyblock.api.service.records.WorldRecordFlag;
import com.bgsoftware.superiorskyblock.api.service.records.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.SpawnerKey;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.service.IService;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class WorldRecordServiceImpl implements WorldRecordService, IService {

    private static final BlockFace[] NEARBY_BLOCKS = new BlockFace[]{
            BlockFace.UP, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST
    };

    private final SuperiorSkyblockPlugin plugin;

    public WorldRecordServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return WorldRecordService.class;
    }

    @Override
    public RecordResult recordBlockPlace(Block block, int blockCount, @Nullable BlockState oldBlockState, WorldRecordFlag recordFlag) {
        Preconditions.checkNotNull(block, "block cannot be null");
        Preconditions.checkNotNull(recordFlag, "recordFlag cannot be null");
        return recordBlockPlace(Keys.of(block), block.getLocation(), blockCount, oldBlockState, recordFlag);
    }

    @Override
    public RecordResult recordBlockPlace(Key blockKey, Location blockLocation, int blockCount,
                                         @Nullable BlockState oldBlockState, WorldRecordFlag recordFlag) {
        Preconditions.checkNotNull(blockKey, "blockKey cannot be null");
        Preconditions.checkNotNull(blockLocation, "blockLocation cannot be null");
        Preconditions.checkNotNull(recordFlag, "recordFlag cannot be null");
        Preconditions.checkArgument(blockLocation.getWorld() != null, "blockLocation's world cannot be null");

        Island island = plugin.getGrid().getIslandAt(blockLocation);

        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        recordBlockPlaceInternal(island, blockKey, blockLocation, blockCount, oldBlockState, recordFlag);
        return RecordResult.SUCCESS;
    }

    @Override
    public RecordResult recordMultiBlocksPlace(KeyMap<Integer> blockCounts, Location location, WorldRecordFlag recordFlag) {
        Preconditions.checkNotNull(blockCounts, "blockCounts cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(recordFlag, "recordFlag cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world cannot be null");

        if (blockCounts.isEmpty())
            return RecordResult.SUCCESS;

        Island island = plugin.getGrid().getIslandAt(location);
        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        island.handleBlocksPlace(blockCounts);

        if (recordFlag.has(WorldRecordFlag.DIRTY_CHUNK)) {
            island.markChunkDirty(location.getWorld(), location.getBlockX() >> 4,
                    location.getBlockZ() >> 4, true);
        }

        return RecordResult.SUCCESS;
    }

    @Override
    public RecordResult recordBlockBreak(Block block, WorldRecordFlag recordFlag) {
        Preconditions.checkNotNull(block, "block cannot be null");
        Preconditions.checkNotNull(recordFlag, "recordFlag cannot be null");
        return recordBlockBreak(block, plugin.getNMSWorld().getDefaultAmount(block), recordFlag);
    }

    @Override
    public RecordResult recordBlockBreak(Block block, int blockCount, WorldRecordFlag recordFlag) {
        Preconditions.checkNotNull(block, "block cannot be null");
        Preconditions.checkNotNull(recordFlag, "recordFlag cannot be null");
        return recordBlockBreak(Keys.of(block), block.getLocation(), blockCount, recordFlag);
    }

    @Override
    public RecordResult recordBlockBreak(Key blockKey, Location blockLocation, int blockCount, WorldRecordFlag recordFlag) {
        Preconditions.checkNotNull(blockKey, "blockKey cannot be null");
        Preconditions.checkNotNull(blockLocation, "blockLocation cannot be null");
        Preconditions.checkNotNull(recordFlag, "recordFlag cannot be null");
        Preconditions.checkArgument(blockLocation.getWorld() != null, "blockLocation's world cannot be null");

        Island island = plugin.getGrid().getIslandAt(blockLocation);

        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        recordBlockBreakInternal(island, blockKey, blockLocation, blockCount, recordFlag);
        return RecordResult.SUCCESS;
    }

    @Override
    public RecordResult recordMultiBlocksBreak(KeyMap<Integer> blockCounts, Location location, WorldRecordFlag recordFlag) {
        return null;
    }

    private void recordBlockPlaceInternal(Island island, Key blockKey, Location blockLocation, int blockCount,
                                          @Nullable BlockState oldBlockState, WorldRecordFlag recordFlag) {
        if (oldBlockState != null && oldBlockState.getType() != Material.AIR) {
            Material blockStateType = oldBlockState.getType();
            Key oldBlockKey;
            int oldBlockCount = 1;

            if (Materials.isLava(blockStateType)) {
                oldBlockKey = ConstantKeys.LAVA;
            } else if (Materials.isWater(blockStateType)) {
                oldBlockKey = ConstantKeys.WATER;
            } else {
                oldBlockKey = Keys.of(oldBlockState);
                oldBlockCount = plugin.getNMSWorld().getDefaultAmount(oldBlockState.getBlock());
            }

            recordBlockBreakInternal(island, oldBlockKey, blockLocation, oldBlockCount, recordFlag);
        }

        if (blockKey.equals(ConstantKeys.END_PORTAL_FRAME_WITH_EYE))
            recordBlockBreakInternal(island, ConstantKeys.END_PORTAL_FRAME, blockLocation, 1, recordFlag);

        if (plugin.getProviders().shouldListenToSpawnerChanges() || !(blockKey instanceof SpawnerKey))
            island.handleBlockPlace(blockKey, blockCount, recordFlag.has(WorldRecordFlag.SAVE_BLOCK_COUNT));

        if (recordFlag.has(WorldRecordFlag.DIRTY_CHUNK)) {
            island.markChunkDirty(blockLocation.getWorld(), blockLocation.getBlockX() >> 4,
                    blockLocation.getBlockZ() >> 4, true);
        }
    }

    private void recordBlockBreakInternal(Island island, Key blockKey, Location blockLocation, int blockCount,
                                          WorldRecordFlag recordFlag) {
        if (plugin.getProviders().shouldListenToSpawnerChanges() || !(blockKey instanceof SpawnerKey))
            island.handleBlockBreak(blockKey, blockCount, recordFlag.has(WorldRecordFlag.SAVE_BLOCK_COUNT));

        boolean handleNearbyBlocks = recordFlag.has(WorldRecordFlag.HANDLE_NEARBY_BLOCKS);
        boolean dirtyChunk = recordFlag.has(WorldRecordFlag.DIRTY_CHUNK);

        if (handleNearbyBlocks || dirtyChunk) {
            EnumMap<BlockFace, Key> nearbyBlocks = new EnumMap<>(BlockFace.class);
            Block block = blockLocation.getBlock();

            if (handleNearbyBlocks) {
                for (BlockFace nearbyFace : NEARBY_BLOCKS) {
                    Block nearbyBlock = block.getRelative(nearbyFace);
                    Material blockType = nearbyBlock.getType();
                    if (blockType != Material.AIR && !blockType.isSolid()) {
                        Key nearbyBlockKey = Keys.of(nearbyBlock);
                        nearbyBlocks.put(nearbyFace, nearbyBlockKey);
                    }
                }
            }

            BukkitExecutor.sync(() -> {
                if (dirtyChunk) {
                    if (plugin.getNMSChunks().isChunkEmpty(block.getChunk())) {
                        island.markChunkEmpty(block.getWorld(), block.getX() >> 4,
                                block.getZ() >> 4, true);
                    }
                }
                if (handleNearbyBlocks) {
                    for (BlockFace nearbyFace : NEARBY_BLOCKS) {
                        Key nearbyBlock = Keys.of(block.getRelative(nearbyFace));
                        Key oldNearbyBlock = nearbyBlocks.getOrDefault(nearbyFace, ConstantKeys.AIR);
                        if (oldNearbyBlock != ConstantKeys.AIR && !nearbyBlock.equals(oldNearbyBlock)) {
                            island.handleBlockBreak(oldNearbyBlock, 1);
                        }
                    }
                }
            }, 2L);
        }
    }

}
