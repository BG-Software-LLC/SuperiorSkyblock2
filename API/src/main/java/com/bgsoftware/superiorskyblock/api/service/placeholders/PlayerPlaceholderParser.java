package com.bgsoftware.superiorskyblock.api.service.placeholders;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * This class represents a player placeholder parser.
 * It should give an output of the parsed value for a player.
 */
public interface PlayerPlaceholderParser extends Function<SuperiorPlayer, String> {

    /**
     * Get the result of this placeholder for the given player.
     *
     * @param superiorPlayer The player that requested the placeholder.
     * @return The parsed result.
     */
    @Override
    @Nullable
    String apply(SuperiorPlayer superiorPlayer);

}
