package com.bgsoftware.superiorskyblock.api.service.portals;

import com.bgsoftware.superiorskyblock.api.events.IslandEnterPortalEvent;

public enum EntityPortalResult {

    /**
     * The player is immuned to portal teleports.
     */
    PLAYER_IMMUNED_TO_PORTAL,

    /**
     * The portal is not inside an island.
     */
    PORTAL_NOT_IN_ISLAND,

    /**
     * The player cannot enter the island the portal leads to.
     */
    DESTINATION_ISLAND_NOT_PERMITTED,

    /**
     * The world the portal leads to is not an islands world.
     */
    DESTINATION_NOT_ISLAND_WORLD,

    /**
     * The world the portal leads to is disabled.
     */
    DESTINATION_WORLD_DISABLED,

    /**
     * The world the portal leads to is not unlocked for the island.
     */
    WORLD_NOT_UNLOCKED,

    /**
     * The schematic for the destination world is being generated.
     */
    SCHEMATIC_GENERATING_COOLDOWN,

    /**
     * The {@link IslandEnterPortalEvent} event that had been fired was cancelled.
     */
    PORTAL_EVENT_CANCELLED,

    /**
     * There is no valid schematic for the destination world.
     */
    INVALID_SCHEMATIC,

    /**
     * The entity went through the portal successfully.
     */
    SUCCEED

}
