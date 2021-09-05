package com.bgsoftware.superiorskyblock.schematic.data;

import com.bgsoftware.superiorskyblock.utils.blocks.BlockChangeTask;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Location;

public final class SchematicBlock {

    public final static SchematicBlock AIR = of(0, (byte) 0, (byte) 0, null, null);

    private final int combinedId;
    private final byte skyLightLevel, blockLightLevel;
    private final CompoundTag statesTag;
    private final CompoundTag tileEntity;

    private SchematicBlock(int combinedId, byte skyLightLevel, byte blockLightLevel, CompoundTag statesTag, CompoundTag tileEntity) {
        this.combinedId = combinedId;
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

    public void applyBlock(BlockChangeTask blockChangeTask, Location location) {
        blockChangeTask.setBlock(location, combinedId, skyLightLevel, blockLightLevel, statesTag, tileEntity);
    }

    public static SchematicBlock of(int combinedId, byte skyLightLevel, byte blockLightLevel, CompoundTag statesTag, CompoundTag tileEntity) {
        return new SchematicBlock(combinedId, skyLightLevel, blockLightLevel, statesTag, tileEntity);
    }

}
