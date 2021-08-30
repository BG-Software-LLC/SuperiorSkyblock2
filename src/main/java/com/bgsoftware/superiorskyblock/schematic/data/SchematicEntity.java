package com.bgsoftware.superiorskyblock.schematic.data;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public final class SchematicEntity {

    private final static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final EntityType entityType;
    private final CompoundTag entityTag;
    private final Location offset;

    private SchematicEntity(EntityType entityType, CompoundTag entityTag, Location offset) {
        this.entityType = entityType;
        this.entityTag = entityTag;
        this.offset = offset;
    }

    public void spawnEntity(Location min) {
        Location location = parseLocation(this.offset, min.getWorld()).add(min);
        plugin.getNMSTags().spawnEntity(entityType, location, entityTag);
    }

    public static SchematicEntity of(EntityType entityType, CompoundTag entityTag, Location offset) {
        return new SchematicEntity(entityType, entityTag, offset);
    }

    private static Location parseLocation(Location location, World world) {
        return new Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

}
