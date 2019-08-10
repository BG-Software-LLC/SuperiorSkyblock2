package com.bgsoftware.superiorskyblock.wrappers;

import org.bukkit.Location;
import org.bukkit.block.Block;

public final class SchematicPosition {

    private int x, y, z;

    private SchematicPosition(int x, int y, int z){
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

    public static SchematicPosition of(String string){
        String[] sections = string.split(",");
        return of(Integer.parseInt(sections[0]), Integer.parseInt(sections[1]), Integer.parseInt(sections[2]));
    }

    public static SchematicPosition of(Block block){
        return of(block.getX(), block.getY(), block.getZ());
    }

    public static SchematicPosition of(int x, int y, int z){
        return new SchematicPosition(x, y, z);
    }

}
