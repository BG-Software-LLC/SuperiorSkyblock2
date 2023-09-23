package com.bgsoftware.superiorskyblock.service.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBlockFlags;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.service.world.RecordResult;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.SpawnerKey;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;

import java.util.EnumMap;

public class WorldRecordServiceImpl implements WorldRecordService, IService {

    @WorldRecordFlags
    private static final int REGULAR_RECORD_FLAGS = WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS;
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
    public RecordResult recordBlockPlace(Block block, int blockCount, @Nullable BlockState oldBlockState,
                                         @WorldRecordFlags int flags) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return recordBlockPlace(Keys.of(block), block.getLocation(), blockCount, oldBlockState, flags);
    }

    @Override
    public RecordResult recordBlockPlace(Key blockKey, Location blockLocation, int blockCount,
                                         @Nullable BlockState oldBlockState, @WorldRecordFlags int flags) {
        Preconditions.checkNotNull(blockKey, "blockKey cannot be null");
        Preconditions.checkNotNull(blockLocation, "blockLocation cannot be null");
        Preconditions.checkArgument(blockLocation.getWorld() != null, "blockLocation's world cannot be null");

        Island island = plugin.getGrid().getIslandAt(blockLocation);

        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        recordBlockPlaceInternal(island, blockKey, blockLocation, blockCount, oldBlockState, flags);
        return RecordResult.SUCCESS;
    }

    @Override
    public RecordResult recordMultiBlocksPlace(KeyMap<Integer> blockCounts, Location location,
                                               @WorldRecordFlags int flags) {
        Preconditions.checkNotNull(blockCounts, "blockCounts cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world cannot be null");

        if (blockCounts.isEmpty())
            return RecordResult.SUCCESS;

        Island island = plugin.getGrid().getIslandAt(location);
        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        boolean saveBlockCounts = (flags & WorldRecordFlags.SAVE_BLOCK_COUNT) != 0;
        boolean dirtyChunks = (flags & WorldRecordFlags.DIRTY_CHUNKS) != 0;

        int blockPlaceFlags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS |
                (saveBlockCounts ? IslandBlockFlags.SAVE_BLOCK_COUNTS : 0);
        island.handleBlocksPlace(blockCounts, blockPlaceFlags);

        if (dirtyChunks) {
            island.markChunkDirty(location.getWorld(), location.getBlockX() >> 4,
                    location.getBlockZ() >> 4, true);
        }

        return RecordResult.SUCCESS;
    }

    private void recordBlockPlaceInternal(Island island, Key blockKey, Location blockLocation, int blockCount,
                                          @Nullable BlockState oldBlockState, @WorldRecordFlags int flags) {
        boolean saveBlockCounts = (flags & WorldRecordFlags.SAVE_BLOCK_COUNT) != 0;
        boolean dirtyChunks = (flags & WorldRecordFlags.DIRTY_CHUNKS) != 0;

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

            recordBlockBreakInternal(island, oldBlockKey, blockLocation, oldBlockCount, flags);
        }

        if (blockKey.equals(ConstantKeys.END_PORTAL_FRAME_WITH_EYE))
            recordBlockBreakInternal(island, ConstantKeys.END_PORTAL_FRAME, blockLocation, 1, flags);

        if (plugin.getProviders().shouldListenToSpawnerChanges() || !(blockKey instanceof SpawnerKey)) {
            int blockPlaceFlags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS;
            if (saveBlockCounts) blockPlaceFlags |= IslandBlockFlags.SAVE_BLOCK_COUNTS;
            island.handleBlockPlace(blockKey, blockCount, blockPlaceFlags);
        }

        if (dirtyChunks) {
            island.markChunkDirty(blockLocation.getWorld(), blockLocation.getBlockX() >> 4,
                    blockLocation.getBlockZ() >> 4, true);
        }
    }

    @Override
    public RecordResult recordBlockBreak(Block block, @WorldRecordFlags int flags) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return recordBlockBreak(block, plugin.getNMSWorld().getDefaultAmount(block), flags);
    }

    @Override
    public RecordResult recordBlockBreak(Block block, int blockCount, @WorldRecordFlags int flags) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return recordBlockBreak(Keys.of(block), block.getLocation(), blockCount, flags);
    }

    @Override
    public RecordResult recordBlockBreak(Key blockKey, Location blockLocation, int blockCount, @WorldRecordFlags int flags) {
        Preconditions.checkNotNull(blockKey, "blockKey cannot be null");
        Preconditions.checkNotNull(blockLocation, "blockLocation cannot be null");
        Preconditions.checkArgument(blockLocation.getWorld() != null, "blockLocation's world cannot be null");

        Island island = plugin.getGrid().getIslandAt(blockLocation);

        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        recordBlockBreakInternal(island, blockKey, blockLocation, blockCount, flags);
        return RecordResult.SUCCESS;
    }

    @Override
    public RecordResult recordMultiBlocksBreak(KeyMap<Integer> blockCounts, Location location,
                                               @WorldRecordFlags int flags) {
        Preconditions.checkNotNull(blockCounts, "blockCounts cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world cannot be null");

        if (blockCounts.isEmpty())
            return RecordResult.SUCCESS;

        Island island = plugin.getGrid().getIslandAt(location);
        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        boolean saveBlockCounts = (flags & WorldRecordFlags.SAVE_BLOCK_COUNT) != 0;
        boolean dirtyChunks = (flags & WorldRecordFlags.DIRTY_CHUNKS) != 0;

        int blockBreakFlags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS |
                (saveBlockCounts ? IslandBlockFlags.SAVE_BLOCK_COUNTS : 0);

        island.handleBlocksBreak(blockCounts, blockBreakFlags);

        if (dirtyChunks && plugin.getNMSChunks().isChunkEmpty(location.getChunk())) {
            island.markChunkEmpty(location.getWorld(), location.getBlockX() >> 4,
                    location.getBlockZ() >> 4, true);
        }

        return RecordResult.SUCCESS;
    }

    private void recordBlockBreakInternal(Island island, Key blockKey, Location blockLocation, int blockCount,
                                          @WorldRecordFlags int flags) {
        boolean saveBlockCounts = (flags & WorldRecordFlags.SAVE_BLOCK_COUNT) != 0;
        boolean dirtyChunks = (flags & WorldRecordFlags.DIRTY_CHUNKS) != 0;
        boolean handleNearbyBlocks = (flags & WorldRecordFlags.HANDLE_NEARBY_BLOCKS) != 0;

        int handleBlockBreakFlag = IslandBlockFlags.UPDATE_LAST_TIME_STATUS |
                (saveBlockCounts ? IslandBlockFlags.SAVE_BLOCK_COUNTS : 0);

        if (plugin.getProviders().shouldListenToSpawnerChanges() || !(blockKey instanceof SpawnerKey))
            island.handleBlockBreak(blockKey, blockCount, handleBlockBreakFlag);

        if (handleNearbyBlocks || dirtyChunks) {
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
                if (dirtyChunks) {
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
                            island.handleBlockBreak(oldNearbyBlock, 1, handleBlockBreakFlag);
                        }
                    }
                }
            }, 2L);
        }
    }

    @Override
    public RecordResult recordEntitySpawn(Entity entity) {
        Preconditions.checkNotNull(entity, "entity parameter cannot be null");

        if (BukkitEntities.canBypassEntityLimit(entity))
            return RecordResult.ENTITY_CANNOT_BE_TRACKED;

        return recordEntitySpawnInternal(entity.getType(), entity.getLocation());
    }

    @Override
    public RecordResult recordEntitySpawn(EntityType entityType, Location location) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null");
        Preconditions.checkNotNull(location, "location parameter cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world parameter cannot be null");

        return recordEntitySpawnInternal(entityType, location);
    }

    private RecordResult recordEntitySpawnInternal(EntityType entityType, Location location) {
        if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class) ||
                !BukkitEntities.canHaveLimit(entityType))
            return RecordResult.ENTITY_CANNOT_BE_TRACKED;

        Island island = plugin.getGrid().getIslandAt(location);

        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        island.getEntitiesTracker().trackEntity(Keys.of(entityType), 1);

        return RecordResult.SUCCESS;
    }

    @Override
    public RecordResult recordEntityDespawn(Entity entity) {
        Preconditions.checkNotNull(entity, "entity parameter cannot be null");

        if (BukkitEntities.canBypassEntityLimit(entity))
            return RecordResult.ENTITY_CANNOT_BE_TRACKED;

        RecordResult recordResult = recordEntityDespawnInternal(entity.getType(), entity.getLocation());
        if (recordResult != RecordResult.SUCCESS)
            return recordResult;

        if (entity instanceof Minecart) {
            if (entity.hasMetadata("SSB-VehicleDestory")) {
                entity.removeMetadata("SSB-VehicleDestory", plugin);
            } else {
                // Vehicle was not registered by VehicleDestroyEvent; We want to register its block break
                Key blockKey = plugin.getNMSAlgorithms().getMinecartBlock((Minecart) entity);
                recordBlockBreak(blockKey, entity.getLocation(), 1, REGULAR_RECORD_FLAGS);
            }
        }

        return RecordResult.SUCCESS;
    }

    @Override
    public RecordResult recordEntityDespawn(EntityType entityType, Location location) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null");
        Preconditions.checkNotNull(location, "location parameter cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world parameter cannot be null");

        return recordEntityDespawnInternal(entityType, location);
    }

    private RecordResult recordEntityDespawnInternal(EntityType entityType, Location location) {
        if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class) ||
                !BukkitEntities.canHaveLimit(entityType))
            return RecordResult.ENTITY_CANNOT_BE_TRACKED;

        Island island = plugin.getGrid().getIslandAt(location);

        if (island == null)
            return RecordResult.NOT_IN_ISLAND;

        island.getEntitiesTracker().untrackEntity(Keys.of(entityType), 1);

        return RecordResult.SUCCESS;
    }

}
