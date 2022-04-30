package com.bgsoftware.superiorskyblock.schematic.data;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.world.blocks.BlockChangeTask;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class SchematicBlock implements Comparable<SchematicBlock> {

    private final int combinedId;
    private final BlockOffset blockOffset;
    private final byte skyLightLevel;
    private final byte blockLightLevel;
    private final CompoundTag statesTag;
    private final CompoundTag tileEntity;

    public SchematicBlock(int combinedId, BlockOffset blockOffset, byte skyLightLevel, byte blockLightLevel,
                          CompoundTag statesTag, CompoundTag tileEntity) {
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

    public CompoundTag getTileEntity() {
        return tileEntity;
    }

    @Override
    public int compareTo(@NotNull SchematicBlock o) {
        int levelCompare = Integer.compare(blockOffset.getOffsetY(), o.blockOffset.getOffsetY());
        if (levelCompare != 0)
            return levelCompare;
        int xCoordCompare = Integer.compare(blockOffset.getOffsetX(), o.blockOffset.getOffsetX());
        return xCoordCompare != 0 ? xCoordCompare : Integer.compare(blockOffset.getOffsetZ(), o.blockOffset.getOffsetZ());
    }

    public void applyBlock(BlockChangeTask blockChangeTask, Location location) {
        blockChangeTask.setBlock(blockOffset.applyToLocation(location), combinedId, skyLightLevel,
                blockLightLevel, statesTag, tileEntity);
    }

}
