package com.bgsoftware.superiorskyblock.api.entity;

import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeySet;

public interface EntityCategory {

    String getName();

    KeySet getEntities();

    IslandPrivilege getSpawnPrivilege();

    IslandPrivilege getDamagePrivilege();

    IslandPrivilege getInteractPrivilege();

    IslandFlag getSpawnerSpawningIslandFlag();

    IslandFlag getNaturalSpawningIslandFlag();

}
