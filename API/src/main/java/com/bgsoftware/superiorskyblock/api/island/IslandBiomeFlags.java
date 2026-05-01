package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.common.annotations.IntType;

/**
 * The integer value element annotated with {@link IslandBiomeFlags} represents flags related to what to update
 * when the biome is changed. It is mainly used within the {@link Island} interface and its methods.
 */
@IntType({IslandBiomeFlags.UPDATE_BLOCKS, IslandBiomeFlags.UPDATE_ALL_DIMENSIONS})
public @interface IslandBiomeFlags {

    /**
     * Indicates an update of the blocks in the dimensions where the biome will be updated.
     */
    int UPDATE_BLOCKS = (1 << 0);

    /**
     * Indicates a biome update in dimensions where the biome will be changed.
     */
    int UPDATE_DIMENSION = (1 << 1);

    /**
     * Indicates a biome update in all dimensions, not just the one in which the biome will be changed.
     */
    int UPDATE_ALL_DIMENSIONS = (1 << 2);
}
