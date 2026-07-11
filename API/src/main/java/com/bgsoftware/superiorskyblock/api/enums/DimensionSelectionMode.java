package com.bgsoftware.superiorskyblock.api.enums;

/**
 * Used to determine the dimension in which the player changes their island's biome.
 */
public enum DimensionSelectionMode {

    /**
     * Always uses the default dimension.
     */
    DEFAULT,

    /**
     * Uses the dimension from the command argument, or the default if invalid.
     */
    ARGUMENT,

    /**
     * Uses the player's current dimension, or the default if is not inside the island world.
     */
    LOCATION,

    /**
     * Prioritizes ARGUMENT, then LOCATION or DEFAULT if the previous ones fail.
     */
    AUTO,

}
