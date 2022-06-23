package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class SchematicEntity {

    private final static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final EntityType entityType;
    private final CompoundTag entityTag;
    private final BlockOffset offset;

    public SchematicEntity(EntityType entityType, CompoundTag entityTag, BlockOffset offset) {
        this.entityType = entityType;
        this.entityTag = entityTag;
        this.offset = offset;
    }

    public void spawnEntity(Location min) {
        plugin.getNMSTags().spawnEntity(entityType, offset.applyToLocation(min), entityTag);
    }

}
