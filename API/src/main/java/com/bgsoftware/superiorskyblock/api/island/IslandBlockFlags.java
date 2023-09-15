package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.common.annotations.IntType;

/**
 * The integer value element annotated with {@link IslandBlockFlags} represents flags related to what to do
 * when block change is recorded. It is mainly used within the {@link Island} interface and its methods.
 */
@IntType({IslandBlockFlags.SAVE_BLOCK_COUNTS, IslandBlockFlags.UPDATE_LAST_TIME_STATUS})
public @interface IslandBlockFlags {

    /**
     * Indicates to save block counts into the DB after the block count change.
     */
    int SAVE_BLOCK_COUNTS = (1 << 0);

    /**
     * Indicates to update the last time the island was updated due to the block count change.
     */
    int UPDATE_LAST_TIME_STATUS = (1 << 1);

}
