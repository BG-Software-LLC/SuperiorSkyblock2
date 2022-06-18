package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;

public class IslandArea {

    private int minX;
    private int minZ;
    private int maxX;
    private int maxZ;

    public IslandArea(BlockPosition center, int size) {
        this(center.getX() - size, center.getZ() - size, center.getX() + size, center.getZ() + size);
    }

    public IslandArea(int minX, int minZ, int maxX, int maxZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }

    public void expand(int size) {
        if (size != 0) {
            this.minX -= size;
            this.minZ -= size;
            this.maxX += size;
            this.maxZ += size;
        }
    }

    public void rshift(int shift) {
        if (shift != 0) {
            this.minX = this.minX >> shift;
            this.minZ = this.minZ >> shift;
            this.maxX = this.maxX >> shift;
            this.maxZ = this.maxZ >> shift;
        }
    }

    public boolean intercepts(int x, int z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
    }

    public IslandArea copy() {
        return new IslandArea(this.minX, this.minZ, this.maxX, this.maxZ);
    }

}
