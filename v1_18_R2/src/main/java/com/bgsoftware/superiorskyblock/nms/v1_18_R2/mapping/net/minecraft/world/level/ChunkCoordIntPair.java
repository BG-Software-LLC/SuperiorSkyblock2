package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.core.BlockPosition;

public class ChunkCoordIntPair extends MappedObject<net.minecraft.world.level.ChunkCoordIntPair> {

    public ChunkCoordIntPair(int chunkX, int chunkZ) {
        this(new net.minecraft.world.level.ChunkCoordIntPair(chunkX, chunkZ));
    }

    public ChunkCoordIntPair(net.minecraft.world.level.ChunkCoordIntPair handle) {
        super(handle);
    }

    public long pair() {
        return handle.a();
    }

    public int getX() {
        return handle.c;
    }

    public int getZ() {
        return handle.d;
    }

}
