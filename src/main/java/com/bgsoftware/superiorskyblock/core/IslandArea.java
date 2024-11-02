package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;

public class IslandArea {

    private double minX;
    private double minZ;
    private double maxX;
    private double maxZ;

    public IslandArea(BlockPosition center, double size) {
        this(center.getX() - size, center.getZ() - size, center.getX() + size, center.getZ() + size);
    }

    public IslandArea(double minX, double minZ, double maxX, double maxZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
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
            this.minX = (int)this.minX >> shift;
            this.minZ = (int)this.minZ >> shift;
            this.maxX = (int)this.maxX >> shift;
            this.maxZ = (int)this.maxZ >> shift;
        }
    }

    public boolean intercepts(double x, double z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
    }

    public IslandArea copy() {
        return new IslandArea(this.minX, this.minZ, this.maxX, this.maxZ);
    }

}
