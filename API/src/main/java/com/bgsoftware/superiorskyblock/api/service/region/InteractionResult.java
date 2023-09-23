package com.bgsoftware.superiorskyblock.api.service.region;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;

public enum InteractionResult {

    /**
     * The interaction was made outside an island.
     */
    OUTSIDE_ISLAND,

    /**
     * The player is missing an {@link IslandPrivilege} for doing the interaction.
     */
    MISSING_PRIVILEGE,

    /**
     * The interaction that was made cannot be done while the island is being recalculated.
     */
    ISLAND_RECALCULATE,

    /**
     * The interaction can be done.
     */
    SUCCESS

}
