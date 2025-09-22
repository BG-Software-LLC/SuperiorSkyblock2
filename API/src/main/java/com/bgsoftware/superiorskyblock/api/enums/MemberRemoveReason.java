package com.bgsoftware.superiorskyblock.api.enums;

/**
 * Used to determine the reason why a member was removed from an island.
 */
public enum MemberRemoveReason {

    /**
     * The member was removed because the island was disbanded.
     */
    DISBAND,

    /**
     * The member was removed because another player kicked them from the island.
     */
    KICK,

    /**
     * The member was removed because they left the island.
     */
    LEAVE

}
