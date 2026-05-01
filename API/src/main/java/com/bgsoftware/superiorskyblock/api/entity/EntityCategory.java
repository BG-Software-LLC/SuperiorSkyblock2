package com.bgsoftware.superiorskyblock.api.entity;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeySet;

public interface EntityCategory {

    /**
     * Get the name of the entity category.
     */
    String getName();

    /**
     * Get the entities this category contains.
     */
    KeySet getEntities();

    /**
     * Get the required {@link IslandPrivilege} to spawn entities from this category on an island.
     */
    @Nullable
    IslandPrivilege getSpawnPrivilege();

    /**
     * Get the required {@link IslandPrivilege} to damage entities from this category on an island.
     */
    @Nullable
    IslandPrivilege getDamagePrivilege();

    /**
     * Get the required {@link IslandPrivilege} to interact with entities from this category on an island.
     */
    @Nullable
    IslandPrivilege getInteractPrivilege();

    /**
     * Get the required {@link IslandFlag} for entities from this category to spawn on an island from a spawner.
     */
    @Nullable
    IslandFlag getSpawnerSpawningIslandFlag();

    /**
     * Get the required {@link IslandFlag} for entities from this category to spawn on an island naturally.
     */
    @Nullable
    IslandFlag getNaturalSpawningIslandFlag();

}
