package com.bgsoftware.superiorskyblock.api.player;

public enum PlayerStatus {

    /**
     * The player is immuned to PvP and cannot be damaged by other players.
     */
    PVP_IMMUNED,

    /**
     * The player cannot be teleported by portals.
     */
    PORTALS_IMMUNED,

    /**
     * The player recently left an island.
     */
    LEAVING_ISLAND,

    /**
     * The player is being teleported by void-teleport.
     */
    VOID_TELEPORT,

    /**
     * The player has no special status.
     */
    @Deprecated
    NONE

}
