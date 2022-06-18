package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.world.BlockChangeTask;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SchematicBlockData implements Comparable<SchematicBlockData> {

    private final int combinedId;
    private final BlockOffset blockOffset;
    private final byte skyLightLevel;
    private final byte blockLightLevel;
    @Nullable
    private final CompoundTag statesTag;
    @Nullable
    private final CompoundTag tileEntity;

    public SchematicBlockData(int combinedId, BlockOffset blockOffset, byte skyLightLevel, byte blockLightLevel,
                              @Nullable CompoundTag statesTag, @Nullable CompoundTag tileEntity) {
        this.combinedId = combinedId;
        this.blockOffset = blockOffset;
        this.skyLightLevel = skyLightLevel;
        this.blockLightLevel = blockLightLevel;
        this.statesTag = statesTag;
        this.tileEntity = tileEntity;
    }

    public int getCombinedId() {
        return combinedId;
    }

    public byte getSkyLightLevel() {
        return skyLightLevel;
    }

    public byte getBlockLightLevel() {
        return blockLightLevel;
    }

    @Nullable
    public CompoundTag getStatesTag() {
        return statesTag;
    }

    @Nullable
    public CompoundTag getTileEntity() {
        return tileEntity;
    }

    @Override
    public int compareTo(@NotNull SchematicBlockData o) {
        int levelCompare = Integer.compare(blockOffset.getOffsetY(), o.blockOffset.getOffsetY());
        if (levelCompare != 0)
            return levelCompare;
        int xCoordCompare = Integer.compare(blockOffset.getOffsetX(), o.blockOffset.getOffsetX());
        return xCoordCompare != 0 ? xCoordCompare : Integer.compare(blockOffset.getOffsetZ(), o.blockOffset.getOffsetZ());
    }

    public void applyBlock(BlockChangeTask blockChangeTask, Location location) {
        blockChangeTask.setBlock(blockOffset.applyToLocation(location), this);
    }

}
