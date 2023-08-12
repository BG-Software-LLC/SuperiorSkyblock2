package com.bgsoftware.superiorskyblock.api.service.world;

public class WorldRecordFlag {

    public static final WorldRecordFlag NONE = new WorldRecordFlag(0);
    public static final WorldRecordFlag DIRTY_CHUNK = new WorldRecordFlag(1 << 0);
    public static final WorldRecordFlag SAVE_BLOCK_COUNT = new WorldRecordFlag(1 << 1);
    public static final WorldRecordFlag HANDLE_NEARBY_BLOCKS = new WorldRecordFlag(1 << 2);

    private final int flag;

    private WorldRecordFlag(int flag) {
        this.flag = flag;
    }

    public boolean has(WorldRecordFlag other) {
        return (this.flag & other.flag) == other.flag;
    }

    public WorldRecordFlag and(WorldRecordFlag other) {
        return new WorldRecordFlag(this.flag & other.flag);
    }

    public WorldRecordFlag or(WorldRecordFlag other) {
        return new WorldRecordFlag(this.flag | other.flag);
    }

    public WorldRecordFlag not(WorldRecordFlag other) {
        return new WorldRecordFlag(this.flag & ~other.flag);
    }

}
