package com.bgsoftware.superiorskyblock.core.schematic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class SchematicEntity {

    private final static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final EntityType entityType;
    private final CompoundTag entityTag;
    private final BlockOffset offset;

    public SchematicEntity(EntityType entityType, CompoundTag entityTag, BlockOffset offset) {
        this.entityType = entityType;
        this.entityTag = SchematicEntityFilter.filterNBTTag(entityTag);
        this.offset = offset;
    }

    public void spawnEntity(Location min) {
        plugin.getNMSTags().spawnEntity(entityType, offset.applyToLocation(min), entityTag);
    }

    public boolean isAlreadySpawned(Location min) {
        Location location = offset.applyToLocation(min);
        World world = location.getWorld();
        if (world == null)
            return false;

        return hasEntityAt(location, world.getNearbyEntities(location, 1, 1, 1));
    }

    public boolean isAlreadySpawned(Location min, Iterable<Entity> chunkEntities) {
        return hasEntityAt(offset.applyToLocation(min), chunkEntities);
    }

    private boolean hasEntityAt(Location location, Iterable<Entity> nearbyEntities) {
        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity.getType() != entityType)
                continue;

            Location entityLocation = nearbyEntity.getLocation();
            if (entityLocation.getBlockX() == location.getBlockX() && entityLocation.getBlockY() == location.getBlockY() &&
                    entityLocation.getBlockZ() == location.getBlockZ())
                return true;
        }

        return false;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Location getLocation(Location min) {
        return offset.applyToLocation(min);
    }

}
