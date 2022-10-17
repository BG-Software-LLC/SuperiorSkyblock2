package com.bgsoftware.superiorskyblock.core;

import java.util.Objects;

public class DirtyChunk {

    private final String worldName;
    private final int x;
    private final int z;

    public DirtyChunk(String worldName, int x, int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirtyChunk that = (DirtyChunk) o;
        return x == that.x && z == that.z && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, z);
    }

}
