package com.bgsoftware.superiorskyblock.wrappers;

import org.bukkit.Location;
import org.bukkit.block.Block;

public final class BlockPosition {

    private int x, y, z;

    private BlockPosition(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
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

    public Location addToLocation(Location location){
        return location.clone().add(x, y, z);
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }

    public static BlockPosition of(String string){
        String[] sections = string.split(",");
        return of(Integer.valueOf(sections[0]), Integer.valueOf(sections[1]), Integer.valueOf(sections[2]));
    }

    public static BlockPosition of(Block block){
        return of(block.getX(), block.getY(), block.getZ());
    }

    public static BlockPosition of(int x, int y, int z){
        return new BlockPosition(x, y, z);
    }

}
