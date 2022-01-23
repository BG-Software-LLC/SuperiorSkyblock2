package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping;

public final class ChunkCoordIntPair extends MappedObject<net.minecraft.world.level.ChunkCoordIntPair> {

    public ChunkCoordIntPair(int chunkX, int chunkZ) {
        this(new net.minecraft.world.level.ChunkCoordIntPair(chunkX, chunkZ));
    }

    public ChunkCoordIntPair(BlockPosition blockPosition) {
        this(new net.minecraft.world.level.ChunkCoordIntPair(blockPosition.getHandle()));
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
