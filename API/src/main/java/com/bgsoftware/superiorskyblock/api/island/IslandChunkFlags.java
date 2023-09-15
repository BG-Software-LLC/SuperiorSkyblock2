package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.common.annotations.IntType;

/**
 * The integer value element annotated with {@link IslandChunkFlags} represents flags related to which chunks
 * to do the action on. It is mainly used within the {@link Island} interface and its methods.
 */
@IntType({IslandChunkFlags.ONLY_PROTECTED, IslandChunkFlags.NO_EMPTY_CHUNKS})
public @interface IslandChunkFlags {

    /**
     * Indicates to only do the action on chunks within the protected-radius of the island.
     */
    int ONLY_PROTECTED = (1 << 0);

    /**
     * Indicates to only do the action on chunks that have blocks inside them.
     * It is generally a good practice to use this flag whenever possible to reduce performance impact.
     */
    int NO_EMPTY_CHUNKS = (1 << 1);

}
