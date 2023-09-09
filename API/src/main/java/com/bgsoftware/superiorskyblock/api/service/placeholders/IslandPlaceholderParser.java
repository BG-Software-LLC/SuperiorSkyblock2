package com.bgsoftware.superiorskyblock.api.service.placeholders;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.function.BiFunction;

/**
 * This class represents an island placeholder parser.
 * It should give an output of the parsed value for an island.
 */
public interface IslandPlaceholderParser extends BiFunction<Island, SuperiorPlayer, String> {

    /**
     * Get the result of this placeholder for the given island.
     *
     * @param island         The island to parse.
     * @param superiorPlayer The player that requested the placeholder.
     * @return The parsed result.
     */
    @Override
    @Nullable
    String apply(@Nullable Island island, SuperiorPlayer superiorPlayer);

}
