package com.bgsoftware.superiorskyblock.schematic.data;

import org.bukkit.block.Block;

public final class SchematicPosition {

    private final int x;
    private final int y;
    private final int z;

    private SchematicPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static SchematicPosition of(String string) {
        String[] sections = string.split(",");
        return of(Integer.parseInt(sections[0]), Integer.parseInt(sections[1]), Integer.parseInt(sections[2]));
    }

    public static SchematicPosition of(Block block) {
        return of(block.getX(), block.getY(), block.getZ());
    }

    public static SchematicPosition of(int x, int y, int z) {
        return new SchematicPosition(x, y, z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }

}
