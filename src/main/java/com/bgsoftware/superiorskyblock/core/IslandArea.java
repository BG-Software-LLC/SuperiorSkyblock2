package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;

/**
 * Represents a rectangle area of an island on the X/Z axis.
 * <p>
 * The area is built from inclusive block coordinates, but is queried using continuous world
 * coordinates, so positions of entities can be checked against it directly. The block {@code maxX}
 * spans the world coordinates {@code [maxX, maxX + 1)} - therefore the area covers everything from
 * {@code minX} (inclusive) up to {@code maxX + 1} (exclusive) on the X axis, and the same on the Z axis.
 * <p>
 * Block coordinates can be passed to the query methods as well, as a block coordinate is the minimum
 * corner of the block it represents.
 */
public class IslandArea {

    private double minX;
    private double minZ;
    private double maxX;
    private double maxZ;

    public void update(BlockPosition center, double size) {
        update(center.getX() - size, center.getZ() - size, center.getX() + size, center.getZ() + size);
    }

    public void update(double minX, double minZ, double maxX, double maxZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }

    public boolean intercepts(double x, double z) {
        return x >= this.minX && x < this.maxX + 1D && z >= this.minZ && z < this.maxZ + 1D;
    }

    public boolean expandAndIntercepts(double x, double z, double expandFactor) {
        if (expandFactor == 0)
            return intercepts(x, z);
        double minX = this.minX - expandFactor;
        double minZ = this.minZ - expandFactor;
        double maxX = this.maxX + 1D + expandFactor;
        double maxZ = this.maxZ + 1D + expandFactor;
        return x >= minX && x < maxX && z >= minZ && z < maxZ;
    }

    public boolean expandRshiftAndIntercepts(int chunkX, int chunkZ, double expandFactor, int shiftFactor) {
        // Chunk coordinates are discrete indexes, therefore the bounds of the expanded area are
        // converted back into inclusive block coordinates before they are shifted into chunks.
        int minChunkX = (int) Math.floor(this.minX - expandFactor) >> shiftFactor;
        int minChunkZ = (int) Math.floor(this.minZ - expandFactor) >> shiftFactor;
        int maxChunkX = (int) Math.floor(this.maxX + expandFactor) >> shiftFactor;
        int maxChunkZ = (int) Math.floor(this.maxZ + expandFactor) >> shiftFactor;
        return chunkX >= minChunkX && chunkX <= maxChunkX && chunkZ >= minChunkZ && chunkZ <= maxChunkZ;
    }

}
