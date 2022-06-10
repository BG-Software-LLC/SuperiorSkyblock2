package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level;

import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;

public final class ChunkCoordIntPair extends MappedObject<net.minecraft.world.level.ChunkCoordIntPair> {

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
        return handle.e;
    }

    public int getZ() {
        return handle.f;
    }

}
