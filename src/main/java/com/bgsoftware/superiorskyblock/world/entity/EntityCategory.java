package com.bgsoftware.superiorskyblock.world.entity;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.google.common.base.Preconditions;

public class EntityCategory {

    private static final KeyMap<EntityCategory> entityToCategories = KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE);

    @Nullable
    private final IslandPrivilege damagePrivilege;
    @Nullable
    private final IslandPrivilege spawnPrivilege;
    @Nullable
    private final IslandFlag spawnerSpawnFlag;
    @Nullable
    private final IslandFlag naturalSpawnFlag;

    private EntityCategory(KeySet entityTypes, @Nullable IslandPrivilege damagePrivilege,
                           @Nullable IslandPrivilege spawnPrivilege, @Nullable IslandFlag spawnerSpawnFlag,
                           @Nullable IslandFlag naturalSpawnFlag) {
        this.damagePrivilege = damagePrivilege;
        this.spawnPrivilege = spawnPrivilege;
        this.spawnerSpawnFlag = spawnerSpawnFlag;
        this.naturalSpawnFlag = naturalSpawnFlag;
        setCategoryForEntities(entityTypes);
    }

    private void setCategoryForEntities(KeySet entityTypes) {
        for (Key entityType : entityTypes) {
            entityToCategories.put(entityType, this);
        }
    }

    @Nullable
    public IslandPrivilege getDamagePrivilege() {
        return damagePrivilege;
    }

    @Nullable
    public IslandPrivilege getSpawnPrivilege() {
        return spawnPrivilege;
    }

    @Nullable
    public IslandFlag getSpawnerSpawnFlag() {
        return spawnerSpawnFlag;
    }

    @Nullable
    public IslandFlag getNaturalSpawnFlag() {
        return naturalSpawnFlag;
    }

    @Nullable
    public static EntityCategory getEntityCategory(Key entityType) {
        return entityToCategories.get(entityType);
    }

    public static EntityCategory register(KeySet entityTypes, @Nullable IslandPrivilege damagePrivilege,
                                          @Nullable IslandPrivilege spawnPrivilege, @Nullable IslandFlag spawnerSpawnFlag,
                                          @Nullable IslandFlag naturalSpawnFlag) {
        Preconditions.checkNotNull(entityTypes, "entityTypes parameter cannot be null.");
        return new EntityCategory(entityTypes, damagePrivilege, spawnPrivilege, spawnerSpawnFlag, naturalSpawnFlag);
    }

    public static void clear() {
        entityToCategories.clear();
    }

}
