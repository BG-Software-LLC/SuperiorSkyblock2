package com.bgsoftware.superiorskyblock.schematics.data;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class SchematicEntity {

    private final static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final EntityType entityType;
    private final CompoundTag entityTag;
    private final SBlockPosition location;

    private SchematicEntity(EntityType entityType, CompoundTag entityTag, SBlockPosition location){
        this.entityType = entityType;
        this.entityTag = entityTag;
        this.location = location;
    }

    public void spawnEntity(Location center){
        Location location = this.location.parse(center.getWorld()).add(center.clone().add(0.5, 1, 0.5));
        LivingEntity livingEntity = (LivingEntity) center.getWorld().spawnEntity(location, entityType);
        plugin.getNMSTags().getFromNBTTag(livingEntity, entityTag);
    }

    public static SchematicEntity of(EntityType entityType, CompoundTag entityTag, SBlockPosition location){
        return new SchematicEntity(entityType, entityTag, location);
    }

}
