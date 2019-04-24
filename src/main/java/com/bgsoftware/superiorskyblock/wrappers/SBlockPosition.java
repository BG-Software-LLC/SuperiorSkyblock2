package com.bgsoftware.superiorskyblock.wrappers;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class SBlockPosition implements BlockPosition {

    private final int x, y, z;
    private final String world;

    public SBlockPosition(String world, int x, int y, int z){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public World getWorld(){
        return Bukkit.getWorld(world);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public Block getBlock(){
        return parse().getBlock();
    }

    @Override
    public Location parse(World world){
        return new Location(world, getX(), getY(), getZ());
    }

    @Override
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
        if(obj instanceof SBlockPosition){
            SBlockPosition other = (SBlockPosition) obj;
            return world.equals(other.world) && x == other.x && y == other.y && z == other.z;
        }
        return super.equals(obj);
    }

    public static SBlockPosition of(String location){
        String[] sections = location.split(", ");
        return of(sections[0], Integer.valueOf(sections[1]), Integer.valueOf(sections[2]), Integer.valueOf(sections[3]));
    }

    public static SBlockPosition of(Location location){
        return of(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static SBlockPosition of(String world, int x, int y, int z){
        return new SBlockPosition(world, x, y, z);
    }

}
