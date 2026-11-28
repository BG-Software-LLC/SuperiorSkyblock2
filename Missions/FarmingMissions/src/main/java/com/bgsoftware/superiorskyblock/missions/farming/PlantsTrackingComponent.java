package com.bgsoftware.superiorskyblock.missions.farming;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectMethod;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class PlantsTrackingComponent {

    private static final ReflectMethod<Integer> WORLD_GET_MIN_HEIGHT = new ReflectMethod<>(
            new ClassInfo("CraftWorld", ClassInfo.PackageType.CRAFTBUKKIT),
            "getMinHeight",
            new ClassInfo[0]);

    private final TrackedPlantsData trackedPlantsData;

    private final int worldMinHeight;

    public PlantsTrackingComponent(World world) {
        this(world, new TrackedPlantsData());
    }

    public PlantsTrackingComponent(World world, TrackedPlantsData trackedPlantsData) {
        this.worldMinHeight = WORLD_GET_MIN_HEIGHT.isValid() ? WORLD_GET_MIN_HEIGHT.invoke(world) : 0;
        this.trackedPlantsData = trackedPlantsData;
    }

    public void track(int x, int y, int z, UUID placer) {
        int block = getBlockIndex(x, y, z);
        trackedPlantsData.track(getChunkKey(x, z), block, placer);
    }

    public void untrack(int x, int y, int z) {
        int block = getBlockIndex(x, y, z);
        trackedPlantsData.untrack(getChunkKey(x, z), block);
    }

    @Nullable
    public UUID getPlacer(int x, int y, int z) {
        int block = getBlockIndex(x, y, z);
        return trackedPlantsData.getPlacer(getChunkKey(x, z), block);
    }

    public Map<Long, Map<Integer, UUID>> getPlants() {
        return this.trackedPlantsData.getPlants();
    }

    private int getBlockIndex(int blockX, int blockY, int blockZ) {
        int chunkMinX = blockX >> 4 << 4;
        int chunkMinZ = blockZ >> 4 << 4;


        byte relativeX = (byte) (blockX - chunkMinX);
        short relativeY = (short) (blockY - this.worldMinHeight);
        byte relativeZ = (byte) (blockZ - chunkMinZ);

        return (relativeY << 8) | (relativeX << 4) | relativeZ;
    }

    private static long getChunkKey(int blockX, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        return (long) chunkX << 32 | chunkZ;
    }

}
