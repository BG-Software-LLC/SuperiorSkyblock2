package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.common.annotations.Nullable;

public interface PlayerRole {

    /**
     * Get the id of the role.
     */
    int getId();

    /**
     * Get the name of the role.
     */
    String getName();

    /**
     * Get the display-name of the role.
     * This is shown in chat, placeholders, etc.
     */
    String getDisplayName();

    /**
     * Get the weight of the role in the ladder.
     */
    int getWeight();

    /**
     * Check whether or not the role is higher than another role.
     *
     * @param role The role to check.
     */
    boolean isHigherThan(PlayerRole role);

    /**
     * Check whether or not the role is less than another role.
     *
     * @param role The role to check.
     */
    boolean isLessThan(PlayerRole role);

    /**
     * Check whether or not the role is the first role in the ladder.
     */
    boolean isFirstRole();

    /**
     * Check whether or not the role is the last role in the ladder.
     */
    boolean isLastRole();

    /**
     * Check whether or not the role is the role is in the ladder.
     */
    boolean isRoleLadder();

    /**
     * Get the next role in the ladder.
     * Will return null if the role is not in the ladder, or it's the last role in the ladder.
     */
    @Nullable
    PlayerRole getNextRole();

    /**
     * Get the previous role in the ladder.
     * Will return null if the role is not in the ladder, or it's the first role in the ladder.
     */
    @Nullable
    PlayerRole getPreviousRole();


}
