package com.bgsoftware.superiorskyblock.api.service.records;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import javax.annotation.Nullable;

public interface WorldRecordService {

    RecordResult recordBlockPlace(Block block, int blockCount, @Nullable BlockState oldBlockState, WorldRecordFlag recordFlag);

    RecordResult recordBlockPlace(Key blockKey, Location blockLocation, int blockCount, @Nullable BlockState oldBlockState, WorldRecordFlag recordFlag);

    RecordResult recordMultiBlocksPlace(KeyMap<Integer> blockCounts, Location location, WorldRecordFlag recordFlag);

    RecordResult recordBlockBreak(Block block, WorldRecordFlag recordFlag);

    RecordResult recordBlockBreak(Block block, int blockCount, WorldRecordFlag recordFlag);

    RecordResult recordBlockBreak(Key blockKey, Location blockLocation, int blockCount, WorldRecordFlag recordFlag);

    RecordResult recordMultiBlocksBreak(KeyMap<Integer> blockCounts, Location location, WorldRecordFlag recordFlag);

}
