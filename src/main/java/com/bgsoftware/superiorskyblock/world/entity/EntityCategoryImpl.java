package com.bgsoftware.superiorskyblock.world.entity;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;

import java.util.HashSet;

public class EntityCategoryImpl implements EntityCategory {

    private final String name;
    private final KeySet entities;
    @Nullable
    private final IslandPrivilege spawnPrivilege;
    @Nullable
    private final IslandPrivilege damagePrivilege;
    @Nullable
    private final IslandPrivilege interactPrivilege;
    @Nullable
    private final IslandFlag spawnerSpawnFlag;
    @Nullable
    private final IslandFlag naturalSpawnFlag;

    public EntityCategoryImpl(String name, KeySet entities, @Nullable IslandPrivilege spawnPrivilege,
                              @Nullable IslandPrivilege damagePrivilege, @Nullable IslandPrivilege interactPrivilege,
                              @Nullable IslandFlag spawnerSpawnFlag, @Nullable IslandFlag naturalSpawnFlag) {
        this.name = name;
        this.entities = KeySets.unmodifiableKeySet(entities);
        this.spawnPrivilege = spawnPrivilege;
        this.damagePrivilege = damagePrivilege;
        this.interactPrivilege = interactPrivilege;
        this.spawnerSpawnFlag = spawnerSpawnFlag;
        this.naturalSpawnFlag = naturalSpawnFlag;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public KeySet getEntities() {
        return this.entities;
    }

    @Override
    public IslandPrivilege getSpawnPrivilege() {
        return this.spawnPrivilege;
    }

    @Override
    public IslandPrivilege getDamagePrivilege() {
        return this.damagePrivilege;
    }

    @Override
    public IslandPrivilege getInteractPrivilege() {
        return this.interactPrivilege;
    }

    @Override
    public IslandFlag getSpawnerSpawningIslandFlag() {
        return this.spawnerSpawnFlag;
    }

    @Override
    public IslandFlag getNaturalSpawningIslandFlag() {
        return this.naturalSpawnFlag;
    }
}
