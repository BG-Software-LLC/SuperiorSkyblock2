package com.bgsoftware.superiorskyblock.api.service.world;

/**
 * Record flags are used to decide how to handle the recording of an action.
 */
public class WorldRecordFlag {

    /**
     * Do nothing special.
     */
    public static final WorldRecordFlag NONE = new WorldRecordFlag(0);

    /**
     * Mark dirty chunks status for changes in chunks.
     * Can be used when there are changes for blocks in the world.
     */
    public static final WorldRecordFlag DIRTY_CHUNK = new WorldRecordFlag(1 << 0);

    /**
     * Save the new block count after the block change.
     * Can be used when there are changes for blocks in the world.
     */
    public static final WorldRecordFlag SAVE_BLOCK_COUNT = new WorldRecordFlag(1 << 1);

    /**
     * Check for nearby block changes.
     * Can be used when there are changes for blocks in the world.
     */
    public static final WorldRecordFlag HANDLE_NEARBY_BLOCKS = new WorldRecordFlag(1 << 2);

    private final int flag;

    private WorldRecordFlag(int flag) {
        this.flag = flag;
    }

    /**
     * Check if this flag has another flag.
     *
     * @param other The flag to check.
     */
    public boolean has(WorldRecordFlag other) {
        return (this.flag & other.flag) == other.flag;
    }

    /**
     * Combine this flag with {@param other}.
     *
     * @param other The other flag to combine.
     * @return The new combined flag.
     */
    public WorldRecordFlag and(WorldRecordFlag other) {
        return new WorldRecordFlag(this.flag | other.flag);
    }

}
