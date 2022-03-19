package com.bgsoftware.superiorskyblock.api.service.placeholders;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public interface IslandPlaceholderParser extends BiFunction<Island, SuperiorPlayer, String> {

    @Override
    @Nullable
    String apply(@Nullable Island island, SuperiorPlayer superiorPlayer);

}
