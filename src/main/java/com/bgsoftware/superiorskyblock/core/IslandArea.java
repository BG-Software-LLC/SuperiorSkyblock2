package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;

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
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
    }

    public boolean expandAndIntercepts(double x, double z, double expandFactor) {
        if (expandFactor == 0)
            return intercepts(x, z);
        double minX = this.minX - expandFactor;
        double minZ = this.minZ - expandFactor;
        double maxX = this.maxX + expandFactor;
        double maxZ = this.maxZ + expandFactor;
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean expandRshiftAndIntercepts(double x, double z, double expandFactor, int shiftFactor) {
        if (shiftFactor == 0)
            return intercepts(x, z);
        int minX = (int) (this.minX - expandFactor) >> shiftFactor;
        int minZ = (int) (this.minZ - expandFactor) >> shiftFactor;
        int maxX = (int) (this.maxX + expandFactor) >> shiftFactor;
        int maxZ = (int) (this.maxZ + expandFactor) >> shiftFactor;
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

}
