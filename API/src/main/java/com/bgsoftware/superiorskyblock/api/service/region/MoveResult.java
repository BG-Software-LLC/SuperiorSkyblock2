package com.bgsoftware.superiorskyblock.api.service.region;

import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;

public enum MoveResult {

    /**
     * The player cannot do the movement as he is banned from the island.
     */
    BANNED_FROM_ISLAND,

    /**
     * The player cannot do the movement as the island is locked to the public.
     */
    ISLAND_LOCKED,

    /**
     * The {@link IslandEnterEvent} event was cancelled.
     */
    ENTER_EVENT_CANCELLED,

    /**
     * The player cannot move out of an island into the wilderness.
     */
    LEAVE_ISLAND_TO_OUTSIDE,

    /**
     * The player was moved too far away while being in island-preview mode.
     */
    ISLAND_PREVIEW_MOVED_TOO_FAR,

    /**
     * The player was teleported due to void-teleport.
     */
    VOID_TELEPORT,

    /**
     * The player can do the movement.
     */
    SUCCESS

}
