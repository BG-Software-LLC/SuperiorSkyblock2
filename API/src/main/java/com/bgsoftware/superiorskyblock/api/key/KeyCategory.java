package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;

public interface KeyCategory {

    /**
     * Get the name of the category.
     */
    String getName();

    /**
     * Check whether the provided key is listed in this category.
     *
     * @param key The key to check.
     */
    boolean isListed(Key key);

    /**
     * Get the required {@link IslandPrivilege} to do the provided action.
     *
     * @param action The action to check.
     */
    @Nullable
    IslandPrivilege getRequiredPrivilege(Action action);

    /**
     * Get the required {@link IslandFlag} to do the provided action.
     *
     * @param action The action to check.
     */
    @Nullable
    IslandFlag getRequiredFlag(Action action);

    enum Action {

        PLACE,

        BREAK,

        INTERACT,

        SPAWN,

        DAMAGE,

        SPAWNER_SPAWN,

        NATURAL_SPAWN

    }

}
