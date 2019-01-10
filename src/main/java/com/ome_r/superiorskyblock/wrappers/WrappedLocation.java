package com.ome_r.superiorskyblock.wrappers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class WrappedLocation {

    private final int x, y, z;
    private final String world;

    private WrappedLocation(World world, int x, int y, int z){
        this.world = world.getName();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld(){
        return Bukkit.getWorld(world);
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

    public Block getBlock(){
        return parse().getBlock();
    }

    public Location parse(){
        return new Location(getWorld(), getX(), getY(), getZ());
    }

    @Override
    public String toString() {
        return world + ", " + x + ", " + y + ", " + z;
    }

    @Override
    public int hashCode() {
        int hash = 19 * 3 + (this.world != null ? this.world.hashCode() : 0);
        hash = 19 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 19 * hash + (int)(Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        hash = 19 * hash + (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WrappedLocation){
            WrappedLocation other = (WrappedLocation) obj;
            return world.equals(other.world) && x == other.x && y == other.y && z == other.z;
        }
        return super.equals(obj);
    }

    public static WrappedLocation of(String location){
        String[] sections = location.split(", ");
        return of(new Location(Bukkit.getWorld(sections[0]), Integer.valueOf(sections[1]), Integer.valueOf(sections[2]), Integer.valueOf(sections[3])));
    }

    public static WrappedLocation of(Location location){
        return new WrappedLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

}
