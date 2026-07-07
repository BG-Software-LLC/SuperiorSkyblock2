package com.bgsoftware.superiorskyblock.missions.blocks;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectMethod;
import org.bukkit.World;

import java.util.Map;

public class BlocksTrackingComponent {

    private static final ReflectMethod<Integer> WORLD_GET_MIN_HEIGHT = new ReflectMethod<>(
            new ClassInfo("CraftWorld", ClassInfo.PackageType.CRAFTBUKKIT),
            "getMinHeight",
            new ClassInfo[0]);

    // Key represents chunk's coords
    // Value represents all blocks broken in that chunk
    private TrackedBlocksData trackedBlocksData = new TrackedBlocksData();

    private final int worldMinHeight;

    public BlocksTrackingComponent(World world) {
        this(!WORLD_GET_MIN_HEIGHT.isValid() ? 0 : WORLD_GET_MIN_HEIGHT.invoke(world));
    }

    public BlocksTrackingComponent(int worldMinHeight) {
        this.worldMinHeight = worldMinHeight;
    }

    public int getWorldMinHeight() {
        return worldMinHeight;
    }

    public void add(int blockX, int blockY, int blockZ) {
        long chunkKey = serializeChunk(blockX >> 4, blockZ >> 4);
        this.trackedBlocksData.track(chunkKey, serializeLocation(blockX, blockY, blockZ));
    }

    public boolean remove(int blockX, int blockY, int blockZ) {
        long chunkKey = serializeChunk(blockX >> 4, blockZ >> 4);
        return this.trackedBlocksData.untrack(chunkKey, () -> serializeLocation(blockX, blockY, blockZ));
    }

    public boolean contains(int blockX, int blockY, int blockZ) {
        long chunkKey = serializeChunk(blockX >> 4, blockZ >> 4);
        return this.trackedBlocksData.contains(chunkKey, () -> serializeLocation(blockX, blockY, blockZ));
    }

    public void loadBlocks(TrackedBlocksData trackedBlocksData) {
        this.trackedBlocksData = trackedBlocksData;
    }

    public Map<Long, ChunkBitSet> getBlocks() {
        return this.trackedBlocksData.getBlocks();
    }

    private int serializeLocation(int blockX, int blockY, int blockZ) {
        int chunkMinX = blockX >> 4 << 4;
        int chunkMinZ = blockZ >> 4 << 4;


        byte relativeX = (byte) (blockX - chunkMinX);
        short relativeY = (short) (blockY - this.worldMinHeight);
        byte relativeZ = (byte) (blockZ - chunkMinZ);

        return (relativeY << 8) | (relativeX << 4) | relativeZ;
    }

    private static long serializeChunk(int chunkX, int chunkZ) {
        return (long) chunkX << 32 | chunkZ;
    }

}
