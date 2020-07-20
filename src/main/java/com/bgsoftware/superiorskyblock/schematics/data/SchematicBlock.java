package com.bgsoftware.superiorskyblock.schematics.data;

import com.bgsoftware.superiorskyblock.utils.blocks.BlockChangeTask;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import org.bukkit.Location;

public final class SchematicBlock {

    public final static SchematicBlock AIR = of(0, null, null);

    private final int combinedId;
    private final CompoundTag statesTag;
    private final CompoundTag tileEntity;

    private SchematicBlock(int combinedId, CompoundTag statesTag, CompoundTag tileEntity){
        this.combinedId = combinedId;
        this.statesTag = statesTag;
        this.tileEntity = tileEntity;
    }

    public int getCombinedId() {
        return combinedId;
    }

    public void applyBlock(BlockChangeTask blockChangeTask, Location location){
        blockChangeTask.setBlock(location, combinedId, statesTag, tileEntity);
    }

    public static SchematicBlock of(int combinedId, CompoundTag statesTag, CompoundTag tileEntity){
        return new SchematicBlock(combinedId, statesTag, tileEntity);
    }

}
