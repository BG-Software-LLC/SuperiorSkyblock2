package com.bgsoftware.superiorskyblock.api.block;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeySet;

public interface BlockCategory {

    /**
     * Get the name of the entity category.
     */
    String getName();

    /**
     * Get the blocks this category contains.
     */
    KeySet getBlocks();

    /**
     * Get the required {@link IslandPrivilege} to place blocks from this category on an island.
     */
    @Nullable
    IslandPrivilege getPlacePrivilege();

    /**
     * Get the required {@link IslandPrivilege} to break blocks from this category on an island.
     */
    @Nullable
    IslandPrivilege getBreakPrivilege();

    /**
     * Get the required {@link IslandPrivilege} to interact with blocks from this category on an island.
     */
    @Nullable
    IslandPrivilege getInteractPrivilege();

}
