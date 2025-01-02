package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.world.WorldInfo;

public class MutableChunkPosition extends ChunkPosition {

    public MutableChunkPosition() {
        super(false);
    }

    public MutableChunkPosition reset(WorldInfo worldInfo, int x, int z) {
        this.worldInfo = worldInfo;
        this.x = x;
        this.z = z;
        this.pairedXZ = -1;
        this.cachedBukkitWorld.clear();
        return this;
    }

}
