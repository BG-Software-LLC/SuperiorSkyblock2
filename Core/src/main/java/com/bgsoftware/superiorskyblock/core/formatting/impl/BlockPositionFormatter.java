package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.formatting.IBiFormatter;

public class BlockPositionFormatter implements IBiFormatter<BlockPosition, WorldInfo> {

    private static final BlockPositionFormatter INSTANCE = new BlockPositionFormatter();

    public static BlockPositionFormatter getInstance() {
        return INSTANCE;
    }

    private BlockPositionFormatter() {

    }

    @Override
    public String format(BlockPosition value, WorldInfo worldInfo) {
        return worldInfo.getName() + ", " + value.getX() + ", " + value.getY() + ", " + value.getZ();
    }
}
