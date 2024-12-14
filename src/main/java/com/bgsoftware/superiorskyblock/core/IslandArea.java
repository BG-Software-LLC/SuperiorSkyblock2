package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;

public class IslandArea implements ObjectsPool.Releasable, AutoCloseable {

    private static final ObjectsPool<IslandArea> POOL = new ObjectsPool<>(IslandArea::new);

    private double minX;
    private double minZ;
    private double maxX;
    private double maxZ;

    public static IslandArea of(BlockPosition center, double size) {
        return of(center, size, true);
    }

    public static IslandArea of(BlockPosition center, double size, boolean fromPool) {
        return of(center.getX() - size, center.getZ() - size, center.getX() + size, center.getZ() + size, fromPool);
    }

    public static IslandArea of(double minX, double minZ, double maxX, double maxZ) {
        return of(minX, minZ, maxX, maxZ, true);
    }

    public static IslandArea of(double minX, double minZ, double maxX, double maxZ, boolean fromPool) {
        IslandArea islandArea = fromPool ? POOL.obtain() : new IslandArea();
        return islandArea.initialize(minX, minZ, maxX, maxZ);
    }

    private IslandArea() {

    }

    private IslandArea initialize(double minX, double minZ, double maxX, double maxZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        return this;
    }

    public void expand(double size) {
        if (size != 0) {
            this.minX -= size;
            this.minZ -= size;
            this.maxX += size;
            this.maxZ += size;
        }
    }

    public void rshift(int shift) {
        if (shift != 0) {
            this.minX = (int) this.minX >> shift;
            this.minZ = (int) this.minZ >> shift;
            this.maxX = (int) this.maxX >> shift;
            this.maxZ = (int) this.maxZ >> shift;
        }
    }

    public boolean intercepts(double x, double z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
    }

    public IslandArea copy() {
        return of(this.minX, this.minZ, this.maxX, this.maxZ);
    }

    @Override
    public void release() {
        this.minX = 0;
        this.minZ = 0;
        this.maxX = 0;
        this.maxZ = 0;
        POOL.release(this);
    }

    @Override
    public void close() {
        release();
    }
}
