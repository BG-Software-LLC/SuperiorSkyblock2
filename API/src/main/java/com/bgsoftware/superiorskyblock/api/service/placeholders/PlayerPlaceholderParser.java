package com.bgsoftware.superiorskyblock.api.service.placeholders;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface PlayerPlaceholderParser extends Function<SuperiorPlayer, String> {

    @Override
    @Nullable
    String apply(SuperiorPlayer superiorPlayer);

}
