package com.bgsoftware.superiorskyblock.api.service.world;

import com.bgsoftware.common.annotations.IntType;

/**
 * The integer value element annotated with {@link WorldRecordFlags} represents flags related to what to do
 * when block change is recorded. It is mainly used within the {@link WorldRecordService} interface and its methods.
 */
@IntType({WorldRecordFlags.SAVE_BLOCK_COUNT, WorldRecordFlags.DIRTY_CHUNKS, WorldRecordFlags.HANDLE_NEARBY_BLOCKS})
public @interface WorldRecordFlags {

    /**
     * Save the new block count after the block change.
     */
    int SAVE_BLOCK_COUNT = 1 << 0;

    /**
     * Mark dirty chunks status for changes in chunks.
     */
    int DIRTY_CHUNKS = 1 << 1;

    /**
     * Check for nearby block changes.
     * Only handled when breaking blocks to check for nearby block changes that
     * were affected by the breaking of the block.
     */
    int HANDLE_NEARBY_BLOCKS = 1 << 2;

}
