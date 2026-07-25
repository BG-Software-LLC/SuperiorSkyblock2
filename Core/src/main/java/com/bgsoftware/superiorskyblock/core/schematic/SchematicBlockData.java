package com.bgsoftware.superiorskyblock.core.schematic;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;

public class SchematicBlockData implements Comparable<SchematicBlockData> {

    private final int combinedId;
    private final BlockOffset blockOffset;
    @Nullable
    private final SchematicBlock.Extra extra;

    public SchematicBlockData(int combinedId, BlockOffset blockOffset, @Nullable SchematicBlock.Extra extra) {
        this.combinedId = combinedId;
        this.blockOffset = blockOffset;
        this.extra = extra;
    }

    public BlockOffset getBlockOffset() {
        return blockOffset;
    }

    public int getCombinedId() {
        return combinedId;
    }

    @Nullable
    public SchematicBlock.Extra getExtra() {
        return extra;
    }

    @Override
    public int compareTo(@NotNull SchematicBlockData o) {
        int levelCompare = Integer.compare(blockOffset.getOffsetY(), o.blockOffset.getOffsetY());
        if (levelCompare != 0)
            return levelCompare;
        int xCoordCompare = Integer.compare(blockOffset.getOffsetX(), o.blockOffset.getOffsetX());
        return xCoordCompare != 0 ? xCoordCompare : Integer.compare(blockOffset.getOffsetZ(), o.blockOffset.getOffsetZ());
    }

}
